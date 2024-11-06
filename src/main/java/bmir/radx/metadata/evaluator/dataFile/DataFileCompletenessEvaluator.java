package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationMetric;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.sharedComponents.CompletionRateChecker;
import bmir.radx.metadata.evaluator.util.FieldCategory;
import bmir.radx.metadata.evaluator.util.TemplateGetter;
import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.BASIC_INFO;
import static bmir.radx.metadata.evaluator.EvaluationCriterion.COMPLETENESS;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.util.FieldCategory.*;

@Component
public class DataFileCompletenessEvaluator {
  private final CompletionRateChecker completionRateChecker;
  private final TemplateGetter templateGetter;

  public DataFileCompletenessEvaluator(CompletionRateChecker completionRateChecker, TemplateGetter templateGetter) {
    this.completionRateChecker = completionRateChecker;
    this.templateGetter = templateGetter;
  }

  public void evaluate(List<TemplateInstanceArtifact> metadataInstances, Consumer<EvaluationResult> consumer){
    var templateSchemaArtifact = templateGetter.getDataFileTemplate();
    Map<FieldCategory, Map<Integer, Integer>> completenessDistribution = new HashMap<>();
    for (var requirement : FieldCategory.values()) {
      completenessDistribution.put(requirement, new HashMap<>());
    }

    if (!metadataInstances.isEmpty()) {
      var templateInstanceValuesReporter = new TemplateInstanceValuesReporter(metadataInstances.get(0));
      var result = completionRateChecker.getSingleDataFileCompleteness(templateSchemaArtifact, templateInstanceValuesReporter);
      int totalRequiredFields = result.totalRequiredFields();
      int totalRecommendedFields = result.totalRecommendedFields();
      int totalOptionalFields = result.totalOptionalFields();
      int totalFields = result.totalFields();

      for (var instance: metadataInstances) {
        templateInstanceValuesReporter = new TemplateInstanceValuesReporter(instance);
        var completionResult = completionRateChecker.getSingleDataFileCompleteness(templateSchemaArtifact, templateInstanceValuesReporter);
        completionRateChecker.updateCompletenessDistribution(completionResult, completenessDistribution);
      }

      Map<EvaluationMetric, Integer> basicInfoResults = Map.of(
          TOTAL_NUMBER_OF_RECORDS, metadataInstances.size(),
          TOTAL_FIELDS_PER_RECORD, totalFields,
          REQUIRED_FIELDS_PER_RECORD, totalRequiredFields,
          RECOMMENDED_FIELDS_PER_RECORD, totalRecommendedFields,
          OPTIONAL_FIELDS_PER_RECORD, totalOptionalFields
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
