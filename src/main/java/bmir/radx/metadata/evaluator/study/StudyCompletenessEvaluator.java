package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.CompletenessContainer;
import bmir.radx.metadata.evaluator.EvaluationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.*;

@Component
public class StudyCompletenessEvaluator {
  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer) {
    List<Integer> incompleteStudies = new ArrayList<>();
    var overallCompleteness = CompletenessContainer.initiateCompletenessMap();
    var nonEmptyRowCount = rows.stream()
        .filter(row -> {
          var isComplete = isCompleteStudyRow(row);
          if (!isComplete) {
            incompleteStudies.add(row.rowNumber());
          }
          return isComplete;
        })
        .count();

    var ratio = ((double) nonEmptyRowCount / rows.size()) * 100;
    consumer.accept(new EvaluationResult(FULL_COMPLETENESS_STUDY_RATIO, String.valueOf(ratio)));
    if (!incompleteStudies.isEmpty()) {
      consumer.accept(new EvaluationResult(INCOMPLETE_STUDY_LIST, incompleteStudies.toString()));
    }

    rows.forEach(row -> {
          var completenessRate = calculateCompletionRate(row);
          CompletenessContainer.updateCompletenessDistribution(completenessRate, overallCompleteness);
            }
        );
    consumer.accept(new EvaluationResult(OVERALL_COMPLETENESS_DISTRIBUTION, overallCompleteness.toString()));
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
        nonEmptyStringCell(row.dccProvidedPublicationUrls()) &&
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
    if (nonEmptyStringCell(row.dccProvidedPublicationUrls())) nonEmptyFields++;
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
