package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.SpreadsheetHeaders;
import bmir.radx.metadata.evaluator.IssueLevel;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.thirdParty.rePORTER.RePORTERService;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import com.tupilabs.human_name_parser.HumanNameParserBuilder;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.ACCURACY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.SpreadsheetHeaders.*;
import static bmir.radx.metadata.evaluator.IssueLevel.REVIEW_NEEDED;

@Component
public class StudyAccuracyEvaluator {
  private final ClinicalTrialsChecker clinicalTrialsChecker;
  private final RePORTERService rePORTERService;


  public StudyAccuracyEvaluator(ClinicalTrialsChecker clinicalTrialsChecker, RePORTERService rePORTERService) {
    this.clinicalTrialsChecker = clinicalTrialsChecker;
    this.rePORTERService = rePORTERService;
  }

  public void evaluate(List<StudyMetadataRow> metadataRows,
                       Consumer<EvaluationResult> consumer,
                       ValidationSummary<SpreadsheetValidationResult> validationSummary){
    var validationResults = validationSummary.getValidationResults();
//    var incorrectCtLinks = clinicalTrialsChecker.checkInvalidClinicalTrialsLink(metadataRows, validationResults);
    var inAccurateStudies = new HashSet<String>();

    for(var row: metadataRows){
      evaluateSingleRecord(row, validationSummary, inAccurateStudies);
    }

    int totalStudies = metadataRows.size();
    int inAccurateStudiesCount = inAccurateStudies.size();
    var rate = (double) (totalStudies - inAccurateStudiesCount) / totalStudies * 100;
    consumer.accept(new EvaluationResult(ACCURACY, ESTIMATED_ACCURATE_STUDY_RATE, rate));
    consumer.accept(new EvaluationResult(ACCURACY, NUMBER_OF_ESTIMATED_INACCURATE_STUDIES, inAccurateStudiesCount));
    if(inAccurateStudiesCount > 0){
      consumer.accept(new EvaluationResult(ACCURACY, ESTIMATED_INACCURATE_STUDIES, inAccurateStudies));
    }
  }

  private void evaluateSingleRecord(StudyMetadataRow row, ValidationSummary<SpreadsheetValidationResult> validationSummary, Set<String> inAccurateStudies){
    var startErrors = validationSummary.getValidationResults().size();
    checkCTLinks(row, validationSummary);
    checkAgainstReporter(row, validationSummary);
    var endErrors = validationSummary.getValidationResults().size();
    if(endErrors > startErrors){
      inAccurateStudies.add(row.studyPHS());
    }
  }

  private Set<String> getIncorrectCtLinks(){
    Set<String> studies = new HashSet<>();
//    studies.add("phs002521");
//    studies.add("phs002584");
//    studies.add("phs002713");
//    studies.add("phs002920");
//    studies.add("phs003359");
    return studies;
  }

  private void checkCTLinks(StudyMetadataRow row, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    var incorrectCtLinks = getIncorrectCtLinks();
    if(incorrectCtLinks.contains(row.studyPHS())){
      String errorMessage = getErrorMessage(CLINICALTRIALS_GOV_URL);
      updateValidationSummary(row.rowNumber(), row.studyPHS(), CLINICALTRIALS_GOV_URL.getHeaderName(), row.clinicalTrialsGovUrl(), null, validationSummary, IssueLevel.ERROR, errorMessage);
    }
  }

  /**
   * This method use NIH Grand Number to retrieve info from RePORTER
   * and check study metadata against retrieved info
   */
  private void checkAgainstReporter(StudyMetadataRow row, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    String nihGrantNumbers = row.nihGrantNumber();
    if (nihGrantNumbers != null) {
      // NIH grant number may be multiple values, split it by `,` and iterate over each
      String[] grantNumbers = nihGrantNumbers.split(",");
      Set<String> foaSet = new HashSet<>();
      Set<String> nihSet = new HashSet<>();
      Set<String> contactPIName = new HashSet<>();
      boolean contactPIFound = false;

      for (String nihGrantNumber : grantNumbers) {
        var response = rePORTERService.sendPostRequest(nihGrantNumber.replaceAll("\\p{Zs}+", " ").trim());
        var results = response.results();

        if (results.isEmpty()) {
          String errorMessage = "Incorrect NIH Grant Number";
          updateValidationSummary(row.rowNumber(), row.studyPHS(), NIH_GRANT_NUMBER.getHeaderName(), row.nihGrantNumber(), null, validationSummary, IssueLevel.ERROR, errorMessage);
          continue;
        } else{
          if (results.size() > 1) {
            System.err.println( row.studyPHS() + " retrieved multiple results from RePORTER using NIH grant number: " + row.nihGrantNumber());
          }

          // Iterate over results to find matching contact PI name and collect all values of FOA number and NIH institution
          for (var result : results) {
            contactPIName.add(result.contactPIName());
            if (sameName(result.contactPIName(), row.contactPiProjectLeader())) {
              contactPIFound  = true;
            }

            // Add FOA Number and NIH to the sets
            if (result.foaNumber() != null) {
              foaSet.add(result.foaNumber());
            }
            if (result.nihic() != null && result.nihic().nih() != null) {
              nihSet.add(result.nihic().nih());
            }
          }
        }
      }

      //Check Contact PI in the study metadata row is in Contact PI Name set
      var contactPINameProvided = row.contactPiProjectLeader();
      if(contactPINameProvided != null && !contactPINameProvided.isEmpty() && !contactPIFound){
        String errorMessage = getErrorMessage(CONTACT_PI_PROJECT_LEADER);
        updateValidationSummary(row.rowNumber(), row.studyPHS(), CONTACT_PI_PROJECT_LEADER.getHeaderName(), contactPINameProvided, String.join("; ", contactPIName), validationSummary, REVIEW_NEEDED, errorMessage);
      }

      // Check if FOA in the study metadata row is in the FOA set
      var foaNumberProvided = row.foaNumber();
      if ( foaNumberProvided!= null && !foaNumberProvided.isEmpty()) {
        Set<String> providedFoaSet = Arrays.stream(foaNumberProvided.split(","))
            .map(String::trim)
            .collect(Collectors.toSet());

        // Find FOA numbers that are not in the FOA set
        Set<String> missingFoaSet = providedFoaSet.stream()
            .filter(providedFoa -> !foaSet.contains(providedFoa))
            .collect(Collectors.toSet());

        if (!missingFoaSet.isEmpty()) {
          String errorMessage = getErrorMessage(FOA_NUMBER);
          updateValidationSummary(row.rowNumber(), row.studyPHS(), FOA_NUMBER.getHeaderName(), foaNumberProvided, String.join("; ", foaSet), validationSummary, IssueLevel.ERROR, errorMessage);
        }
      }

      // Check if NIH institution in the study metadata matches exactly with the NIH set
      var nihICProvided = row.nihInstituteOrCenter();
      Set<String> rowNIHSet = Arrays.stream(nihICProvided.split(","))
          .map(String::trim)
          .collect(Collectors.toSet());
      if (!nihICProvided.isEmpty() && !rowNIHSet.equals(nihSet)) {
        String errorMessage = getErrorMessage(NIH_INSTITUTE_OR_CENTER);
        updateValidationSummary(row.rowNumber(), row.studyPHS(), NIH_INSTITUTE_OR_CENTER.getHeaderName(), row.nihInstituteOrCenter(), String.join("; ", nihSet), validationSummary, IssueLevel.ERROR, errorMessage);
      }
    }
  }

  private boolean isIdentical(String v1, String v2){
    return (v1 == null && v2 == null) || (v1 != null && v1.equals(v2));
  }

  private boolean sameName(String n1, String n2){
    var builder1 = new HumanNameParserBuilder(n1);
    var parser1 = builder1.build();
    String firstName1 = parser1.getFirst();
    String lastName1 = parser1.getLast();

    var builder2 = new HumanNameParserBuilder(n2);
    var parser2 = builder2.build();
    String firstName2 = parser2.getFirst();
    String lastName2 = parser2.getLast();

    return isIdentical(firstName1.toLowerCase(), firstName2.toLowerCase())
        && isIdentical(lastName1.toLowerCase(), lastName2.toLowerCase());
  }

  private void updateValidationSummary(Integer rowNumber, String studyPHS, String columnName, String columnValue, String suggestion, ValidationSummary<SpreadsheetValidationResult> validationSummary, IssueLevel issueLevel, String message){
      validationSummary.addInvalidMetadata(studyPHS);
      var result = new SpreadsheetValidationResult(
          IssueTypeMapping.IssueType.ACCURACY,
          columnName,
          rowNumber,
          studyPHS,
          suggestion,
          columnValue,
          issueLevel,
          message);
      validationSummary.updateValidationResult(result);
  }

  private String getErrorMessage(SpreadsheetHeaders spreadsheetHeaders){
    return "Incorrect " + spreadsheetHeaders.getHeaderName();
  }
}
