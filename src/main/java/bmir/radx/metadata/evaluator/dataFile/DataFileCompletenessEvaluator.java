package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationMetric;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.sharedComponents.CompletionRateChecker;
import bmir.radx.metadata.evaluator.util.FieldRequirement;
import bmir.radx.metadata.evaluator.util.TemplateGetter;
import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.BASIC_INFO;
import static bmir.radx.metadata.evaluator.EvaluationCriterion.COMPLETENESS;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.util.FieldRequirement.*;

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
    Map<FieldRequirement, Map<Integer, Integer>> completenessDistribution = new HashMap<>();
    for (var requirement : FieldRequirement.values()) {
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
          TOTAL_NUMBER_OF_DATA_FILES, metadataInstances.size(),
          TOTAL_FIELDS, totalFields,
          TOTAL_REQUIRED_FIELDS, totalRequiredFields,
          TOTAL_RECOMMENDED_FIELDS, totalRecommendedFields,
          TOTAL_OPTIONAL_FIELDS, totalOptionalFields
      );
      basicInfoResults.forEach((key, value) -> consumer.accept(new EvaluationResult(BASIC_INFO, key, value)));

      Map<FieldRequirement, EvaluationMetric> completenessKeys = Map.of(
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
