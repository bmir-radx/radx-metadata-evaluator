package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.sharedComponents.CompletionRateChecker;
import bmir.radx.metadata.evaluator.sharedComponents.FieldRequirement;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.*;
import static bmir.radx.metadata.evaluator.sharedComponents.FieldRequirement.*;

@Component
public class StudyCompletenessEvaluator {
  private final CompletionRateChecker completionRateChecker;
  private final StudyTemplateGetter studyTemplateGetter;


  public StudyCompletenessEvaluator(CompletionRateChecker completionRateChecker, StudyTemplateGetter studyTemplateGetter) {
    this.completionRateChecker = completionRateChecker;
    this.studyTemplateGetter = studyTemplateGetter;
  }

  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer) {
    var templateSchemaArtifact = studyTemplateGetter.getTemplate();

    Map<FieldRequirement, Map<Integer, Integer>> completenessDistribution = new HashMap<>();
    for (var requirement : FieldRequirement.values()) {
      completenessDistribution.put(requirement, new HashMap<>());
    }

    int totalRequiredFields = 0;
    int totalOptionalFields = 0;
    int totalRecommendedFields = 0;
    for(var row: rows){
      var result = completionRateChecker.checkCompletionRate(row, templateSchemaArtifact);
      totalRequiredFields = result.totalRequiredFields();
      totalRecommendedFields = result.totalRecommendedFields();
      totalOptionalFields = result.totalOptionalFields();

      Map<FieldRequirement, Integer> filledFieldsMap = new HashMap<>();
      filledFieldsMap.put(REQUIRED, result.filledRequiredFields());
      filledFieldsMap.put(RECOMMENDED, result.filledRecommendedFields());
      filledFieldsMap.put(OPTIONAL, result.filledOptionalFields());

      for (var requirement : FieldRequirement.values()) {
        int filledFields = filledFieldsMap.get(requirement);
        Map<Integer, Integer> distributionMap = completenessDistribution.get(requirement);
        distributionMap.put(
            filledFields,
            distributionMap.getOrDefault(filledFields, 0) + 1
        );
      }
    }

    int totalFields = totalRequiredFields + totalRecommendedFields + totalOptionalFields;
    consumer.accept(new EvaluationResult(TOTAL_NUMBER_OF_STUDIES, String.valueOf(rows.size())));
    consumer.accept(new EvaluationResult(TOTAL_FIELDS, String.valueOf(totalFields)));
    consumer.accept(new EvaluationResult(TOTAL_REQUIRED_FIELDS, String.valueOf(totalRequiredFields)));
    consumer.accept(new EvaluationResult(REQUIRED_FIELDS_COMPLETENESS_DISTRIBUTION, completenessDistribution.get(REQUIRED).toString()));
    consumer.accept(new EvaluationResult(TOTAL_RECOMMENDED_FIELDS, String.valueOf(totalRecommendedFields)));
    consumer.accept(new EvaluationResult(RECOMMENDED_FIELDS_COMPLETENESS_DISTRIBUTION, completenessDistribution.get(RECOMMENDED).toString()));
    consumer.accept(new EvaluationResult(TOTAL_OPTIONAL_FIELDS, String.valueOf(totalOptionalFields)));
    consumer.accept(new EvaluationResult(OPTIONAL_FIELDS_COMPLETENESS_DISTRIBUTION, completenessDistribution.get(OPTIONAL).toString()));

//    List<Integer> incompleteStudies = new ArrayList<>();
//    var overallCompleteness = new HashMap<String, Integer>();
//    var nonEmptyRowCount = rows.stream()
//        .filter(row -> {
//          var isComplete = isCompleteStudyRow(row);
//          if (!isComplete) {
//            incompleteStudies.add(row.rowNumber());
//          }
//          return isComplete;
//        })
//        .count();
//
//    var ratio = ((double) nonEmptyRowCount / rows.size()) * 100;
//    consumer.accept(new EvaluationResult(FULL_COMPLETENESS_STUDY_RATIO, String.format("%.2f%%",ratio)));
//    if (!incompleteStudies.isEmpty()) {
//      consumer.accept(new EvaluationResult(INCOMPLETE_STUDY_ROWS, incompleteStudies.toString()));
//    }
//
//    rows.forEach(row -> {
//          var completenessRate = calculateCompletionRate(row);
//          CompletenessContainer.updateDistribution(completenessRate, overallCompleteness);
//            }
//        );
//    consumer.accept(new EvaluationResult(OVERALL_COMPLETENESS_DISTRIBUTION, overallCompleteness.toString()));
//
//    // Calculate completeness rate for each row and update overallCompleteness map
//    rows.forEach(row -> {
//      var completenessRate = calculateCompletionRate(row);
//      updateDistribution(completenessRate, overallCompleteness);
//    });
//
//    consumer.accept(new EvaluationResult(OVERALL_COMPLETION_RATE, overallCompleteness.toString()));
  }

  private boolean isCompleteStudyRow(StudyMetadataRow row) {
    return nonEmptyStringCell(row.studyProgram()) &&
        nonEmptyStringCell(row.studyPHS()) &&
        nonEmptyStringCell(row.studyTitle()) &&
        nonEmptyStringCell(row.description()) &&
        nonEmptyStringCell(row.radxAcknowledgements()) &&
        nonEmptyStringCell(row.nihGrantNumber()) &&
        nonEmptyStringCell(row.rapidsLink()) &&
        nonEmptyDateCell(row.studyStartDate()) &&
        nonEmptyDateCell(row.studyEndDate()) &&
        nonEmptyDateCell(row.studyReleaseDate()) &&
        nonEmptyDateCell(row.updatedAt()) &&
        nonEmptyStringCell(row.foaNumber()) &&
        nonEmptyStringCell(row.foaUrl()) &&
        nonEmptyStringCell(row.contactPiProjectLeader()) &&
        nonEmptyStringCell(row.studyDoi()) &&
        nonEmptyStringCell(row.cdccProvidedPublicationUrls()) &&
        nonEmptyStringCell(row.clinicalTrialsGovUrl()) &&
        nonEmptyStringCell(row.studyWebsiteUrl()) &&
        nonEmptyStringCell(row.studyDesign()) &&
        nonEmptyStringCell(row.dataTypes()) &&
        nonEmptyStringCell(row.studyDomain()) &&
        nonEmptyStringCell(row.nihInstituteOrCenter()) &&
        nonEmptyBooleanCell(row.multiCenterStudy()) &&
        nonEmptyStringCell(row.multiCenterSites()) &&
        nonEmptyStringCell(row.keywords()) &&
        nonEmptyStringCell(row.dataCollectionMethod()) &&
        nonEmptyIntCell(row.estimatedCohortSize()) &&
        nonEmptyStringCell(row.studyPopulationFocus()) &&
        nonEmptyStringCell(row.species()) &&
        nonEmptyStringCell(row.consentDataUseLimitations()) &&
        nonEmptyStringCell(row.studyStatus()) &&
        nonEmptyBooleanCell(row.hasDataFiles());
  }

  private double calculateCompletionRate(StudyMetadataRow row) {
    int totalFields = 32;
    int nonEmptyFields = 0;

    if (nonEmptyStringCell(row.studyProgram())) nonEmptyFields++;
    if (nonEmptyStringCell(row.studyPHS())) nonEmptyFields++;
    if (nonEmptyStringCell(row.studyTitle())) nonEmptyFields++;
    if (nonEmptyStringCell(row.description())) nonEmptyFields++;
    if (nonEmptyStringCell(row.radxAcknowledgements())) nonEmptyFields++;
    if (nonEmptyStringCell(row.nihGrantNumber())) nonEmptyFields++;
    if (nonEmptyStringCell(row.rapidsLink())) nonEmptyFields++;
    if (nonEmptyDateCell(row.studyStartDate())) nonEmptyFields++;
    if (nonEmptyDateCell(row.studyEndDate())) nonEmptyFields++;
    if (nonEmptyDateCell(row.studyReleaseDate())) nonEmptyFields++;
    if (nonEmptyDateCell(row.updatedAt())) nonEmptyFields++;
    if (nonEmptyStringCell(row.foaNumber())) nonEmptyFields++;
    if (nonEmptyStringCell(row.foaUrl())) nonEmptyFields++;
    if (nonEmptyStringCell(row.contactPiProjectLeader())) nonEmptyFields++;
    if (nonEmptyStringCell(row.studyDoi())) nonEmptyFields++;
    if (nonEmptyStringCell(row.cdccProvidedPublicationUrls())) nonEmptyFields++;
    if (nonEmptyStringCell(row.clinicalTrialsGovUrl())) nonEmptyFields++;
    if (nonEmptyStringCell(row.studyWebsiteUrl())) nonEmptyFields++;
    if (nonEmptyStringCell(row.studyDesign())) nonEmptyFields++;
    if (nonEmptyStringCell(row.dataTypes())) nonEmptyFields++;
    if (nonEmptyStringCell(row.studyDomain())) nonEmptyFields++;
    if (nonEmptyStringCell(row.nihInstituteOrCenter())) nonEmptyFields++;
    if (nonEmptyBooleanCell(row.multiCenterStudy())) nonEmptyFields++;
    if (nonEmptyStringCell(row.multiCenterSites())) nonEmptyFields++;
    if (nonEmptyStringCell(row.keywords())) nonEmptyFields++;
    if (nonEmptyStringCell(row.dataCollectionMethod())) nonEmptyFields++;
    if (nonEmptyIntCell(row.estimatedCohortSize())) nonEmptyFields++;
    if (nonEmptyStringCell(row.studyPopulationFocus())) nonEmptyFields++;
    if (nonEmptyStringCell(row.species())) nonEmptyFields++;
    if (nonEmptyStringCell(row.consentDataUseLimitations())) nonEmptyFields++;
    if (nonEmptyStringCell(row.studyStatus())) nonEmptyFields++;
    if (nonEmptyBooleanCell(row.hasDataFiles())) nonEmptyFields++;

    return (double) nonEmptyFields / totalFields * 100;
  }

  private void updateCompletenessDistribution(double completenessRate, Map<String, Integer> overallCompleteness) {
    int range = (int) (completenessRate / 10) * 10; // Calculate range as 0, 10, 20, ..., 90
    String key = range + "-" + (range + 10) + "%"; // Format the key as "0-10%", "10-20%", etc.
    overallCompleteness.merge(key, 1, Integer::sum); // Increment the count for the corresponding range
  }

  private boolean nonEmptyStringCell(String value) {
    return value != null && !value.isEmpty();
  }

  private boolean nonEmptyDateCell(Date value) {
    return value != null;
  }

  private boolean nonEmptyBooleanCell(Boolean value) {
    return value != null;
  }

  private boolean nonEmptyIntCell(Integer value) {
    return value != null;
  }

  private String getRange(double rate) {
    if (rate >= 0 && rate < 25) {
      return "0%-25%";
    } else if (rate >= 25 && rate < 50) {
      return "25%-50%";
    } else if (rate >= 50 && rate < 75) {
      return "50%-75%";
    } else if (rate >= 75 && rate <= 100) {
      return "75%-100%";
    } else {
      throw new IllegalArgumentException("Rate out of expected range: " + rate);
    }
  }
}
