package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.InstanceArtifactValueGetter;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
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
  private static final String titlePath = "/Data File Titles[0]/Title";
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

    //check Title is not Study Name
    checkTitle(templateInstanceArtifacts, inaccurateInstances, validationSummary);

    int totalDataFiles = templateInstanceArtifacts.size();
    int inaccurateInstancesCount = inaccurateInstances.size();
    var rate = (double) (totalDataFiles - inaccurateInstancesCount) / totalDataFiles * 100;
    consumer.accept(new EvaluationResult(ACCURACY, ACCURACY_RATE, rate));
    consumer.accept(new EvaluationResult(ACCURACY, NUMBER_OF_INACCURATE_RECORDS, inaccurateInstancesCount));
    consumer.accept(new EvaluationResult(ACCURACY, INACCURATE_DATA_FILES, inaccurateInstances));
  }

  private void checkTitle(Map<Path, TemplateInstanceArtifact> templateInstanceArtifacts,
                     Set<String> inaccurateInstances,
                     ValidationSummary<JsonValidationResult> validationSummary){
    for(var instanceEntry : templateInstanceArtifacts.entrySet()){
      var instance = instanceEntry.getValue();
      var path = instanceEntry.getKey();
      var title = InstanceArtifactValueGetter.getTitle(instance);
      var studyName = InstanceArtifactValueGetter.getStudyName(instance);
      var phs = InstanceArtifactValueGetter.getStudyPhs(instance);
      int count = 0;
      if(title != null && title.equals(studyName)){
        count +=1;
        inaccurateInstances.add(phs);
        validationSummary.addInvalidMetadata(phs);
        validationSummary.updateValidationResult(
            new JsonValidationResult(
                phs,
                path.getFileName().toString(),
                title,
                IssueTypeMapping.IssueType.ACCURACY,
                "Should be the title of the data file not the study",
                null,
                title)
        );
      }
    }
  }
}
