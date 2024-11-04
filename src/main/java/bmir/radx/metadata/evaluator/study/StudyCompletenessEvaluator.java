package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.EvaluationMetric;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
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

  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer) {
    var templateSchemaArtifact = templateGetter.getStudyTemplate();

    Map<FieldCategory, Map<Integer, Integer>> completenessDistribution = completionRateChecker.initializeCompletenessDistribution();

    if (!rows.isEmpty()) {
      var result = completionRateChecker.getSpreadsheetRowCompleteness(rows.get(0), templateSchemaArtifact);
      int totalRequiredFields = result.totalRequiredFields();
      int totalRecommendedFields = result.totalRecommendedFields();
      int totalOptionalFields = result.totalOptionalFields();
      int totalFields = result.totalFields();

      for (var row : rows) {
        result = completionRateChecker.getSpreadsheetRowCompleteness(row, templateSchemaArtifact);
        completionRateChecker.updateCompletenessDistribution(result, completenessDistribution);
      }

      Map<EvaluationMetric, Integer> basicInfoResults = Map.of(
          TOTAL_NUMBER_OF_RECORDS, rows.size(),
          TOTAL_FIELDS, totalFields,
          TOTAL_REQUIRED_FIELDS, totalRequiredFields,
          TOTAL_RECOMMENDED_FIELDS, totalRecommendedFields,
          TOTAL_OPTIONAL_FIELDS, totalOptionalFields
      );
      basicInfoResults.forEach((key, value) -> consumer.accept(new EvaluationResult(BASIC_INFO, key, value)));

      Map<FieldCategory, EvaluationMetric> completenessKeys = Map.of(
          REQUIRED, REQUIRED_FIELDS_COMPLETENESS_DISTRIBUTION,
          RECOMMENDED, RECOMMENDED_FIELDS_COMPLETENESS_DISTRIBUTION,
          OPTIONAL, OPTIONAL_FIELDS_COMPLETENESS_DISTRIBUTION,
          OVERALL, OVERALL_COMPLETENESS_DISTRIBUTION
      );
      completenessKeys.forEach((requirement, key) ->
          consumer.accept(new EvaluationResult(COMPLETENESS, key, completenessDistribution.get(requirement)))
      );
    }
  }
}
