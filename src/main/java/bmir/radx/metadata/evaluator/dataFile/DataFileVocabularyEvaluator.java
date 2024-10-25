package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.util.ReporterGetter;
import edu.stanford.bmir.radx.metadata.validator.lib.FieldValues;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.VOCABULARIES_DISTRIBUTION;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.sharedComponents.DistributionManager.updateDistribution;

@Component
public class DataFileVocabularyEvaluator {
  private final FieldsCollector fieldsCollector;
  private final ReporterGetter reporterGetter;

  public DataFileVocabularyEvaluator(ReporterGetter reporterGetter) {
    this.reporterGetter = reporterGetter;
    fieldsCollector = new FieldsCollector();
  }

  public void evaluate(List<TemplateInstanceArtifact> templateInstanceArtifacts, Consumer<EvaluationResult> handler){
    var templateReporter = reporterGetter.getTemplateReporter();
    var ctFrequency = new HashMap<String, Integer>();
    var filledCtCountsFrequency = new HashMap<Integer, Integer>();
    for(var instance : templateInstanceArtifacts){
      var instanceReporter = reporterGetter.getTemplateInstanceValuesReporter(instance);
      evaluateSingleDataFile(templateReporter, instanceReporter, ctFrequency, filledCtCountsFrequency);
    }

    handler.accept(new EvaluationResult(VOCABULARIES_DISTRIBUTION, FILLED_CONTROLLED_TERMS_COUNT_DISTRIBUTION, filledCtCountsFrequency.toString()));
    handler.accept(new EvaluationResult(VOCABULARIES_DISTRIBUTION, CONTROLLED_TERMS_DISTRIBUTION, ctFrequency.toString()));
  }

  private void evaluateSingleDataFile(TemplateReporter templateReporter,
                                     TemplateInstanceValuesReporter valuesReporter,
                                     Map<String, Integer> ctFrequency,
                                     Map<Integer, Integer> filledCtCountsFrequency){
    //todo check it is a valid controlled term
    var values = valuesReporter.getValues();
    int filledCtCounts = 0;

    for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
      var path = fieldEntry.getKey();
      var fieldValue = fieldEntry.getValue();
      var valueConstraint = templateReporter.getValueConstraints(path);

      if(!fieldsCollector.isEmptyField(fieldValue) && valueConstraint.isPresent() && valueConstraint.get().isControlledTermValueConstraint()){
        filledCtCounts++;
        var prefLabel = fieldValue.label();
        if(prefLabel.isPresent() && !prefLabel.get().isEmpty()){
          updateDistribution(prefLabel.get(), ctFrequency);
        }
      }
    }

    updateDistribution(filledCtCounts, filledCtCountsFrequency);
  }
}
