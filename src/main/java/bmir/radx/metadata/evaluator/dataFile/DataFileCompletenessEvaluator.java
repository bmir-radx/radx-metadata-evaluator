package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationMetric;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.sharedComponents.CompletionRateChecker;
import bmir.radx.metadata.evaluator.util.FieldCategory;
import bmir.radx.metadata.evaluator.util.InstanceArtifactValueGetter;
import bmir.radx.metadata.evaluator.util.StudyPhsGetter;
import bmir.radx.metadata.evaluator.util.TemplateGetter;
import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
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
  private final StudyPhsGetter studyPhsGetter;

  public DataFileCompletenessEvaluator(CompletionRateChecker completionRateChecker, TemplateGetter templateGetter, StudyPhsGetter studyPhsGetter) {
    this.completionRateChecker = completionRateChecker;
    this.templateGetter = templateGetter;
    this.studyPhsGetter = studyPhsGetter;
  }

  public void evaluate(Map<Path, TemplateInstanceArtifact> metadataInstances, Consumer<EvaluationResult> consumer, ValidationSummary<JsonValidationResult> validationSummary){
    var templateSchemaArtifact = templateGetter.getDataFileTemplate();
    Map<FieldCategory, Map<Integer, Integer>> completenessDistribution = new HashMap<>();
    Map<FieldCategory, Map<String, List<Double>>> completeness = new HashMap<>();
    for (var requirement : FieldCategory.values()) {
      completenessDistribution.put(requirement, new HashMap<>());
    }

    if (!metadataInstances.isEmpty()) {
      for (var instanceEntry: metadataInstances.entrySet()) {
        var instance = instanceEntry.getValue();
        var filePath = instanceEntry.getKey();
        var fileName = filePath.getFileName().toString();
        var templateInstanceValuesReporter = new TemplateInstanceValuesReporter(instance);
        String phs = studyPhsGetter.getCleanStudyPhs(instance);
        var completionResult = completionRateChecker.getSingleDataFileCompleteness(templateSchemaArtifact, templateInstanceValuesReporter);
        completionRateChecker.updateCompletenessDistribution(completionResult, completenessDistribution);
        completionRateChecker.updateCompleteness(completionResult, completeness, phs);
        completionRateChecker.add2Database(completionResult, phs, fileName, validationSummary);
      }

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
    }
  }
}
