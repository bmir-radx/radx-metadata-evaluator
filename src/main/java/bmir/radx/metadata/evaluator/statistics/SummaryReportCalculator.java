package bmir.radx.metadata.evaluator.statistics;

import bmir.radx.metadata.evaluator.*;
import bmir.radx.metadata.evaluator.result.IssueDatabase;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationResult;
import bmir.radx.metadata.evaluator.util.FieldCategory;

import java.util.*;
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
   *     "study": {
   *       "issueType1": 3,
   *       "issueType2": 1,
   *       "overall completeness": 20.0%
   *     },
   *     "data file": {
   *       "issueType1": 1
   *     }
   *   },
   *   "study phs304": {
   *     ...
   *   }
   * }
   */
  public static Map<String, Map<String, Map<String, Object>>> convert2SummaryReportFormat(
      Map<MetadataEntity, EvaluationReport<? extends ValidationResult>> reports) {

    //convert to issues database
    var issuesDatabase = convert2IssueDatabaseFormat(reports);

    // Outer map to hold the final grouping: Study PHS -> Metadata Entity -> Issue Type -> Number
    Map<String, Map<String, Map<String, Object>>> groupedResults = new HashMap<>();

    //Group issues database by study and by issueType.
    for (var record : issuesDatabase) {
      if (!IssueLevel.ERROR.getLevel().equalsIgnoreCase(record.issueLevel())) {
        continue; // Skip non-Error issues
      }
      var phs       = record.phs();
      var issueType = record.issueType();
      var entity    = record.entityType();
      // Ensure we have an entry for this study PHS
      groupedResults.computeIfAbsent(phs, k -> new HashMap<>());
      // Within that PHS, ensure we have an entry for this metadata entity
      var entityMap = groupedResults.get(phs).computeIfAbsent(entity, k -> new HashMap<>());
      var currentCount = 0;
      if (entityMap.containsKey(issueType)) {
        currentCount = (int) entityMap.get(issueType);
      }
      entityMap.put(issueType, currentCount + 1);
    }

    // Add completeness result
    for(var reportEntry: reports.entrySet()){
      var entity = reportEntry.getKey();
      var report = reportEntry.getValue();
      var evaluationResults = report.evaluationResults();
      for(var evaluationResult: evaluationResults){
        if (evaluationResult.getEvaluationCriteria().equals(EvaluationCriterion.COMPLETENESS)){
          var allInstancesCompleteness = evaluationResult.getContent();
          if(evaluationResult.getEvaluationMetric().equals(EvaluationMetric.OVERALL_COMPLETENESS)){
            mergeCompleteness2IssuesDatabase((Map<String, List<Double>>) allInstancesCompleteness, groupedResults, entity, EvaluationMetric.OVERALL_COMPLETENESS);
          } else if (evaluationResult.getEvaluationMetric().equals(EvaluationMetric.REQUIRED_FIELDS_COMPLETENESS)) {
            mergeCompleteness2IssuesDatabase((Map<String, List<Double>>) allInstancesCompleteness, groupedResults, entity, EvaluationMetric.REQUIRED_FIELDS_COMPLETENESS);
          } else if (evaluationResult.getEvaluationMetric().equals(EvaluationMetric.RECOMMENDED_FIELDS_COMPLETENESS)) {
            mergeCompleteness2IssuesDatabase((Map<String, List<Double>>) allInstancesCompleteness, groupedResults, entity, EvaluationMetric.RECOMMENDED_FIELDS_COMPLETENESS);
          } else if (evaluationResult.getEvaluationMetric().equals(EvaluationMetric.OPTIONAL_FIELDS_COMPLETENESS)) {
            mergeCompleteness2IssuesDatabase((Map<String, List<Double>>) allInstancesCompleteness, groupedResults, entity, EvaluationMetric.OPTIONAL_FIELDS_COMPLETENESS);
          }
        }
      }
    }
    return groupedResults;
  }

  private static void mergeCompleteness2IssuesDatabase(Map<String, List<Double>> completeness, Map<String, Map<String, Map<String, Object>>> groupedResults, MetadataEntity metadataEntity, EvaluationMetric evaluationMetric){
    for(var completenessEntry: completeness.entrySet()){
      var phs = completenessEntry.getKey();
      var multiCompleteness = completenessEntry.getValue();
      var averageCompleteness = averageCompleteness(multiCompleteness);
      // Ensure there's a map for this PHS
      groupedResults.computeIfAbsent(phs, k -> new HashMap<>());

      // Within that PHS, ensure there's a map for this MetadataEntity
      var entityMap = groupedResults.get(phs).computeIfAbsent(metadataEntity.getEntityName(), k -> new HashMap<>());

      entityMap.put(evaluationMetric.getDisplayName(), averageCompleteness);
    }
  }

  private static double averageCompleteness(List<Double> values){
    if (values == null || values.isEmpty()) {
      return 0.0;
    }
    return values.stream()
        .mapToDouble(Double::doubleValue)
        .average()
        .orElse(0.0);
  }

  public static List<IssueDatabase.IssueDatabaseRecord> convert2IssueDatabaseFormat(
      Map<MetadataEntity, EvaluationReport<? extends ValidationResult>> reports) {

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

    // Sort groupedResults: "Error" before "Review Needed", then others
    groupedResults.sort(Comparator.comparingInt(record -> {
      String issueType = record.issueLevel();
      switch (issueType) {
        case "Error":
          return 0;
        case "Review Needed":
          return 1;
        default:
          return 2;
      }
    }));


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
