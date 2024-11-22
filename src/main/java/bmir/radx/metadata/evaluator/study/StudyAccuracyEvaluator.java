package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.ACCURACY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.HeaderName.CLINICALTRIALS_GOV_URL;

@Component
public class StudyAccuracyEvaluator {
  private final ClinicalTrialsChecker clinicalTrialsChecker;

  public StudyAccuracyEvaluator(ClinicalTrialsChecker clinicalTrialsChecker) {
    this.clinicalTrialsChecker = clinicalTrialsChecker;
  }

  public void evaluate(List<StudyMetadataRow> metadataRows,
                       Consumer<EvaluationResult> consumer,
                       ValidationSummary<SpreadsheetValidationResult> validationSummary){
    var validationResults = validationSummary.getValidationResults();
//    var incorrectCtLinks = clinicalTrialsChecker.checkInvalidClinicalTrialsLink(metadataRows, validationResults);
    int totalStudies = metadataRows.size();
    var incorrectCtLinks = getIncorrectCtLinks();
    int inAccurateStudies = incorrectCtLinks.size();
    var rate = (double) (totalStudies - inAccurateStudies) / totalStudies * 100;
    consumer.accept(new EvaluationResult(ACCURACY, ESTIMATED_ACCURATE_STUDY_RATE, rate));
    consumer.accept(new EvaluationResult(ACCURACY, NUMBER_OF_ESTIMATED_INACCURATE_STUDIES, inAccurateStudies));
    if(inAccurateStudies > 0){
      consumer.accept(new EvaluationResult(ACCURACY, ESTIMATED_INACCURATE_STUDIES, incorrectCtLinks));
      updateValidationSummary(metadataRows, validationSummary);
    }
  }

  private Set<String> getIncorrectCtLinks(){
    Set<String> studies = new HashSet<>();
    studies.add("phs002521");
    studies.add("phs002584");
    studies.add("phs002713");
    studies.add("phs002920");
    studies.add("phs003359");
    return studies;
  }

  private void updateValidationSummary(List<StudyMetadataRow> metadataRows,
                                       ValidationSummary<SpreadsheetValidationResult> validationSummary){
    var incorrectStudies = getIncorrectCtLinks();
    for(var row: metadataRows){
      if(incorrectStudies.contains(row.studyPHS())){
        validationSummary.addInvalidMetadata(row.studyPHS());
        var result = new SpreadsheetValidationResult(
            IssueTypeMapping.IssueType.INACCURATE_FIELD,
            CLINICALTRIALS_GOV_URL.getHeaderName(),
            row.rowNumber(),
            row.studyPHS(),
            null,
            row.clinicalTrialsGovUrl());
        validationSummary.updateValidationResult(result);
      }
    }
  }
}
