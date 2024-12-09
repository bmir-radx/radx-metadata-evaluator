package bmir.radx.metadata.evaluator.statistics;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.IssueLevel;
import bmir.radx.metadata.evaluator.MetadataEntity;
import bmir.radx.metadata.evaluator.result.ValidationResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
  public static Map<String, Map<MetadataEntity, Map<String, List<String>>>> groupByStudyPhsAndEntities(
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


}
