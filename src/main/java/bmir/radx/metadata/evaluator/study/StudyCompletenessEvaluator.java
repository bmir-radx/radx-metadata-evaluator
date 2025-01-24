package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.EvaluationMetric;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.sharedComponents.CompletionRateChecker;
import bmir.radx.metadata.evaluator.util.FieldCategory;
import bmir.radx.metadata.evaluator.util.TemplateGetter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.BASIC_INFO;
import static bmir.radx.metadata.evaluator.EvaluationCriterion.COMPLETENESS;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.util.FieldCategory.*;

@Component
public class StudyCompletenessEvaluator {
  private final CompletionRateChecker completionRateChecker;
  private final TemplateGetter templateGetter;


  public StudyCompletenessEvaluator(CompletionRateChecker completionRateChecker, TemplateGetter templateGetter) {
    this.completionRateChecker = completionRateChecker;
    this.templateGetter = templateGetter;
  }

  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, ValidationSummary<SpreadsheetValidationResult> validationSummary) {
    var templateSchemaArtifact = templateGetter.getStudyTemplate();

    Map<FieldCategory, Map<Integer, Integer>> completenessDistribution = completionRateChecker.initializeCompletenessDistribution();
    Map<FieldCategory, Map<String, List<Double>>> completeness = new HashMap<>();

    if (!rows.isEmpty()) {
      var result = completionRateChecker.getSpreadsheetRowCompleteness(rows.get(0), templateSchemaArtifact);
      int totalRequiredFields = result.totalRequiredFields();
      int totalRecommendedFields = result.totalRecommendedFields();
      int totalOptionalFields = result.totalOptionalFields();
      int totalFields = result.totalFields();

      for (var row : rows) {
        result = completionRateChecker.getSpreadsheetRowCompleteness(row, templateSchemaArtifact);
        completionRateChecker.updateCompletenessDistribution(result, completenessDistribution);
        completionRateChecker.updateCompleteness(result, completeness, row.studyPHS());
        completionRateChecker.add2Database(result, row.studyPHS(), row.rowNumber(), validationSummary);
      }

      Map<EvaluationMetric, Integer> basicInfoResults = Map.of(
          TOTAL_NUMBER_OF_RECORDS, rows.size(),
          TOTAL_FIELDS_PER_RECORD, totalFields,
          REQUIRED_FIELDS_PER_RECORD, totalRequiredFields,
          RECOMMENDED_FIELDS_PER_RECORD, totalRecommendedFields,
          OPTIONAL_FIELDS_PER_RECORD, totalOptionalFields
      );
      basicInfoResults.forEach((key, value) -> consumer.accept(new EvaluationResult(BASIC_INFO, key, value)));

      // Completeness
      Map<FieldCategory, EvaluationMetric> completenessKeys = Map.of(
          REQUIRED, REQUIRED_FIELDS_COMPLETENESS,
          RECOMMENDED, RECOMMENDED_FIELDS_COMPLETENESS,
          OPTIONAL, OPTIONAL_FIELDS_COMPLETENESS,
          OVERALL, OVERALL_COMPLETENESS
      );
      completenessKeys.forEach((requirement, key) ->
          consumer.accept(new EvaluationResult(COMPLETENESS, key, completeness.get(requirement)))
      );

      Map<FieldCategory, EvaluationMetric> completenessDistributionKeys = Map.of(
          REQUIRED, REQUIRED_FIELDS_COMPLETENESS_DISTRIBUTION,
          RECOMMENDED, RECOMMENDED_FIELDS_COMPLETENESS_DISTRIBUTION,
          OPTIONAL, OPTIONAL_FIELDS_COMPLETENESS_DISTRIBUTION,
          OVERALL, OVERALL_COMPLETENESS_DISTRIBUTION
      );
      completenessDistributionKeys.forEach((requirement, key) ->
          consumer.accept(new EvaluationResult(COMPLETENESS, key, completenessDistribution.get(requirement)))
      );
    }
  }
}
