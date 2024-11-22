package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.ACCURACY;
import static bmir.radx.metadata.evaluator.EvaluationCriterion.CONSISTENCY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

@Component
public class DataFileAccuracyEvaluator {
  private final StudyDataFileCrossEvaluator studyDataFileCrossEvaluator;

  public DataFileAccuracyEvaluator(StudyDataFileCrossEvaluator studyDataFileCrossEvaluator) {
    this.studyDataFileCrossEvaluator = studyDataFileCrossEvaluator;
  }

  public void evaluate(Optional<Path> studyPath,
                       Map<Path, TemplateInstanceArtifact> templateInstanceArtifacts,
                       Consumer<EvaluationResult> consumer,
                       ValidationSummary<JsonValidationResult> validationSummary){
    Set<String> inaccurateInstances = new HashSet<>();

    //cross-check study metadata vs data file metadata if study path is provided
    studyPath.ifPresent(path -> studyDataFileCrossEvaluator.evaluate(path, inaccurateInstances, templateInstanceArtifacts, validationSummary));

    int totalDataFiles = templateInstanceArtifacts.size();
    int inaccurateInstancesCount = inaccurateInstances.size();
    var rate = (double) (totalDataFiles - inaccurateInstancesCount) / totalDataFiles * 100;
    consumer.accept(new EvaluationResult(ACCURACY, ACCURACY_RATE, rate));
    consumer.accept(new EvaluationResult(ACCURACY, NUMBER_OF_INACCURATE_RECORDS, inaccurateInstancesCount));
    consumer.accept(new EvaluationResult(ACCURACY, INACCURATE_DATA_FILES, inaccurateInstances));
  }
}
