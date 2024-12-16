package bmir.radx.metadata.evaluator.statistics;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.IssueLevel;
import bmir.radx.metadata.evaluator.MetadataEntity;
import bmir.radx.metadata.evaluator.result.IssueDatabase;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static bmir.radx.metadata.evaluator.result.IssueDatabase.convertToIssueDatabase;

public class SummaryReportCalculator {
  /**
   * This method generate map using in summary report
   * For example:
   * {
   *   "study phs992": {
   *     "study": {
   *       "issueType1": ["uuid1", "uuid2"],
   *       "issueType2": ["uuid4"]
   *     },
   *     "data file": {
   *       "issueType1": ["uuid3"]
   *     }
   *   },
   *   "study phs304": {
   *     ...
   *   }
   * }
   */
  public static Map<String, Map<MetadataEntity, Map<String, List<String>>>> groupByStudyPhsAndIssueTypes(
      Map<MetadataEntity, EvaluationReport<? extends ValidationResult>> reports) {

    // Outer map to hold the final grouping: Study PHS -> Metadata Entity -> Issue Type -> UUIDs
    Map<String, Map<MetadataEntity, Map<String, List<String>>>> groupedResults = new HashMap<>();

    // Iterate over each report
    reports.forEach((metadataEntity, evaluationReport) -> {
      // Filter validation results for ERROR issue level
      List<? extends ValidationResult> errorResults = evaluationReport.validationResults().stream()
          .filter(result -> result.issueLevel() == IssueLevel.ERROR)
          .toList();

      // Group filtered results by study PHS
      Map<String, Map<String, List<String>>> phsGroupedResults = errorResults.stream()
          .collect(Collectors.groupingBy(
              ValidationResult::studyPhs, // Group by study PHS
              Collectors.groupingBy(
                  result -> result.issueType().getName(),
                  Collectors.mapping(ValidationResult::uuid, Collectors.toList()) // Collect UUIDs
              )
          ));

      // Merge grouped results into the final map under the corresponding metadata entity
      phsGroupedResults.forEach((studyPhs, issuesByType) -> {
        groupedResults.computeIfAbsent(studyPhs, k -> new HashMap<>())
            .put(metadataEntity, issuesByType);
      });
    });

    return groupedResults;
  }

  /**
   * This method generate map using in summary report
   * For example:
   * {
   *   "study phs992": {
   *     "study": [issue1, issue2],
   *     "data file": [issue1, issue2],
   *   "study phs304": {
   *     ...
   *   }
   * }
   */
  public static List<IssueDatabase.IssueDatabaseRecord> groupByStudyPhs(
      Map<MetadataEntity, EvaluationReport<? extends ValidationResult>> reports) {

    // Map to hold the final grouping: Study PHS -> List of Validation Results
    List<IssueDatabase.IssueDatabaseRecord> groupedResults = new ArrayList<>();
    for(var reportEntrySet: reports.entrySet()){
      var entity = reportEntrySet.getKey();
      var report = reportEntrySet.getValue();
      if(entity.equals(MetadataEntity.STUDY_METADATA)){
        var issueReports = report.validationResults();
        for(var issue : issueReports){
          groupedResults.add(convertToIssueDatabase((SpreadsheetValidationResult) issue));
        }
      } else if (entity.equals(MetadataEntity.DATA_FILE_METADATA)) {
        var issueReports = report.validationResults();
        for(var issue: issueReports){
          groupedResults.add(convertToIssueDatabase((JsonValidationResult) issue));
        }
      }
    }

    return groupedResults;
  }


  /**
   * This method returns a map where each metadata entity is associated with a list of all issue types present in its validation results.
   *
   * For example:
   * {
   *   MetadataEntity1: ["issueType1", "issueType2"],
   *   MetadataEntity2: ["issueType1"],
   *   ...
   * }
   */
  public static <T extends ValidationResult> Map<MetadataEntity, List<String>> getAllIssueTypesByMetadataEntity(
      Map<MetadataEntity, EvaluationReport<T>> reports) {

    // Map to store the result: Metadata Entity -> List of Issue Types
    Map<MetadataEntity, List<String>> issueTypesByEntity = new HashMap<>();

    // Iterate through each report
    reports.forEach((metadataEntity, evaluationReport) -> {
      // Collect all distinct issue types from validation results
      List<String> issueTypes = evaluationReport.validationResults().stream()
          .map(result -> result.issueType().getName()) // Extract issue type name
          .distinct() // Ensure uniqueness
          .collect(Collectors.toList());

      // Add to the result map
      issueTypesByEntity.put(metadataEntity, issueTypes);
    });

    return issueTypesByEntity;
  }

}
