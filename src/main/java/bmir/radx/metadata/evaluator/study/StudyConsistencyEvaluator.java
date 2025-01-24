package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.CONSISTENCY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.SpreadsheetHeaders.*;

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

//      checkAcknowledgementStatement(row, validationSummary);
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

  private void checkAcknowledgementStatement(StudyMetadataRow row, ValidationSummary<SpreadsheetValidationResult> validationSummary) {
    String regex = "This study was supported through funding, ([-\\w ]+), (?:for|from) the ([ \\w()]+) as part of the (RADx[- \\w]+) program\\.[^\\n]*?\\s+" +
        "(?:Approved users should acknowledge the provision of data access by dbGaP for accession (phs[\\w]+)\\.v1\\.p1,? and " +
        "(?:the NIH RADx Data Hub|RAPIDS \\((https://rapids\\.ll\\.mit\\.edu/10\\.57895/[-a-z\\d]+)\\))\\. )?" +
        "Approved users should also acknowledge the specific version\\(s\\) of the dataset\\(s\\) obtained from (?:the NIH RADx Data Hub|RAPIDS)\\.";

    Pattern pattern = Pattern.compile(regex);
    Matcher matcher = pattern.matcher(row.radxAcknowledgements());

    if (matcher.find()) {
      // Extract groups
      String fundingSources = matcher.group(1);
      String institutesStatement = matcher.group(2);
      String radxProgram = matcher.group(3);
      String dbGapAccession = matcher.group(4);
      String rapidsLink = matcher.group(5);

      // heck NIH Grant or Contract Numbers
      List<String> grantStatementList = Arrays.asList(fundingSources.split(" and "));
      List<String> grantList = Arrays.asList(row.nihGrantNumber().split(","));
      Set<String> grantSet = new HashSet<>(grantList);
      Set<String> grantStatementSet = new HashSet<>(grantStatementList);

      if (!grantSet.equals(grantStatementSet)) {
        validationSummary.updateValidationResult(
            new SpreadsheetValidationResult(
                IssueTypeMapping.IssueType.ACCURACY,
                RADX_ACKNOWLEDGEMENTS.getHeaderName(),
                row.rowNumber(),
                row.studyPHS(),
                grantSet.toString(),
                row.radxAcknowledgements(),
                String.format("NIH Grant or Contract Number(s) field (%s) does not match Acknowledgement Statement (%s)",
                    String.join(", ", grantList), String.join(", ", grantStatementList))
            )
        );
      }

      // Check NIH Institute/Center
      List<String> centerStatementList = new ArrayList<>();
      Matcher centerMatcher = Pattern.compile("\\(([A-Z]+)\\)").matcher(institutesStatement);
      while (centerMatcher.find()) {
        centerStatementList.add(centerMatcher.group(1));
      }
      List<String> centerList = Arrays.asList(row.nihInstituteOrCenter().split(","));
      Set<String> centerSet = new HashSet<>();
      for (String center : centerList) {
        centerSet.add(center.trim());
      }
      Set<String> centerStatementSet = new HashSet<>();
      for (String center : centerStatementList) {
        centerStatementSet.add(center.trim());
      }

      if (!centerStatementSet.isEmpty() && !centerSet.equals(centerStatementSet)) {
        validationSummary.updateValidationResult(
            new SpreadsheetValidationResult(
                IssueTypeMapping.IssueType.ACCURACY,
                RADX_ACKNOWLEDGEMENTS.getHeaderName(),
                row.rowNumber(),
                row.studyPHS(),
                centerSet.toString(),
                row.radxAcknowledgements(),
                String.format("NIH Institute/Center field (%s) does not match Acknowledgement Statement (%s)",
                    String.join(", ", centerList), String.join(", ", centerStatementList))
            )
        );
      }

      // Check RADx Data Program
      if(radxProgram!= null && !row.studyProgram().equals(radxProgram.trim())){
        validationSummary.updateValidationResult(
            new SpreadsheetValidationResult(
                IssueTypeMapping.IssueType.ACCURACY,
                RADX_ACKNOWLEDGEMENTS.getHeaderName(),
                row.rowNumber(),
                row.studyPHS(),
                row.studyProgram(),
                row.radxAcknowledgements(),
                String.format("RADx Data Program field (%s) does not match Acknowledgement Statement (%s)",
                    row.studyPHS(), radxProgram))
            );
      }

      // Check study phs
      if(dbGapAccession!= null && !row.studyPHS().equals(dbGapAccession.trim())){
        validationSummary.updateValidationResult(
            new SpreadsheetValidationResult(
                IssueTypeMapping.IssueType.ACCURACY,
                RADX_ACKNOWLEDGEMENTS.getHeaderName(),
                row.rowNumber(),
                row.studyPHS(),
                row.studyPHS(),
                row.radxAcknowledgements(),
                String.format("dbGaP Study Accession field (%s) does not match Acknowledgement Statement (%s)",
                    row.studyPHS(), dbGapAccession))
        );
      }
    }
  }

  private SpreadsheetValidationResult getResult(StudyMetadataRow row, String field, String suggestion, String errorMessage){
    return new SpreadsheetValidationResult(IssueTypeMapping.IssueType.CONSISTENCY,
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
