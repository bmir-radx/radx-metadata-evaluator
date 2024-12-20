package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.CONSISTENCY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.SpreadsheetHeaders.*;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.INCONSISTENCY;

@Component
public class StudyConsistencyEvaluator {
  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    List<String> inconsistentRows = new ArrayList<>();
    var validationResults = validationSummary.getValidationResults();
    for(var row: rows){
      if (!multiCenterConsistent(row)){
        inconsistentRows.add(row.studyPHS());
        validationSummary.addInvalidMetadata(row.studyPHS());
        var errorMessage = getErrorMessage(MULTI_CENTER_STUDY.getHeaderName(), MULTI_CENTER_SITES.getHeaderName());
        String suggestion = getMultiCenterStudySuggestion(row.multiCenterSites());
        validationResults.add(getResult(row, MULTI_CENTER_STUDY.getHeaderName(), suggestion, errorMessage));
      }
      if(!sampleSizeIsInRange(row)){
        inconsistentRows.add(row.studyPHS());
        validationSummary.addInvalidMetadata(row.studyPHS());
        String errorMessage = getErrorMessage(ESTIMATED_PARTICIPANT_RANGE.getHeaderName(), ESTIMATED_COHORT_SIZE.getHeaderName());
        String suggestion = getRangeSuggestion(row.estimatedCohortSize());
        validationResults.add(getResult(row, ESTIMATED_PARTICIPANT_RANGE.getHeaderName(), suggestion, errorMessage));
      }
    }
    int totalStudies = rows.size();
    int inconsistentStudies = inconsistentRows.size();
    var rate = (double) (totalStudies - inconsistentStudies) / totalStudies * 100;
    consumer.accept(new EvaluationResult(CONSISTENCY, CONSISTENT_RECORD_RATE, rate));
    consumer.accept(new EvaluationResult(CONSISTENCY, NUMBER_OF_INCONSISTENT_RECORDS, inconsistentStudies));
    if(inconsistentStudies > 0){
      consumer.accept(new EvaluationResult(CONSISTENCY, INCONSISTENT_STUDIES, inconsistentRows));
    }
  }

  private boolean multiCenterConsistent(StudyMetadataRow row){
    var isMultiCenterStudy = isMultiCenterStudy(row.multiCenterStudy());
    return isMultiCenterStudy == isMultiSitesString(row.multiCenterSites());
  }

  private Boolean isMultiCenterStudy(String value){
    return value.equals("Yes");
  }

  private Boolean isMultiSitesString(String sites){
    if(sites == null){
      return false;
    }
    var lowerCaseSites = sites.toLowerCase();
    return lowerCaseSites.contains("and") || lowerCaseSites.contains(";");
  }

  private boolean sampleSizeIsInRange(StudyMetadataRow row){
    Integer size = row.estimatedCohortSize();
    String range = row.estimatedParticipantRange();
    if(size!=null && range!= null && !range.isEmpty()){
      // Handle the special case for "No Participants"
      if (range.equalsIgnoreCase("No Participants")) {
        return size == 0;
      }

      // Handle ranges like "1 - 250", "251 - 500", etc.
      if (range.contains("-")) {
        String[] parts = range.split("-");
        if (parts.length == 2) {
          try {
            int lowerBound = Integer.parseInt(parts[0].trim());
            int upperBound = Integer.parseInt(parts[1].trim());
            return size >= lowerBound && size <= upperBound;
          } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid range format: " + range);
          }
        }
      }

      // Handle ranges like "> 5000"
      if (range.startsWith(">")) {
        try {
          int lowerBound = Integer.parseInt(range.substring(1).trim());
          return size > lowerBound;
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("Invalid range format: " + range);
        }
      }

      throw new IllegalArgumentException("Unknown range format: " + range);
    }
    return true;
  }

  private SpreadsheetValidationResult getResult(StudyMetadataRow row, String field, String suggestion, String errorMessage){
    return new SpreadsheetValidationResult(INCONSISTENCY,
        field,
        row.rowNumber(),
        row.studyPHS(),
        suggestion,
        row.multiCenterStudy(),
        errorMessage
    );
  }

  private String getErrorMessage(String f1, String f2){
    return String.format("'%s' is inconsistent with '%s'", f1, f2);
  }

  private String getMultiCenterStudySuggestion(String sites){
    if(isMultiSitesString(sites)){
      return "Yes";
    } else{
      return "No";

    }
  }

  private String getRangeSuggestion(Integer sampleSize){
    if(sampleSize == null){
      return "No estimated sample size is provided";
    } else if (sampleSize == 0) {
      return "No Participants";
    } else if (sampleSize >= 1 && sampleSize <= 250) {
      return "1 - 250";
    } else if (sampleSize >= 251 && sampleSize <= 500) {
      return "251 - 500";
    } else if (sampleSize >= 501 && sampleSize <= 1000) {
      return "501 - 1000";
    } else if (sampleSize >= 1001 && sampleSize <= 2000) {
      return "1001 - 2000";
    } else if (sampleSize >= 2001 && sampleSize <= 5000) {
      return "2001 - 5000";
    } else if (sampleSize > 5000) {
      return "> 5000";
    } else {
      throw new IllegalArgumentException("Sample size is invalid: " + sampleSize);
    }
  }
}
