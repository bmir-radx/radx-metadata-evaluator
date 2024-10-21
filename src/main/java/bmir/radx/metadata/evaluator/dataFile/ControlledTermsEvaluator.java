package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import edu.stanford.bmir.radx.metadata.validator.lib.FieldValues;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.VOCABULARIES_DISTRIBUTION;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

@Component
public class ControlledTermsEvaluator {
  private final FieldsCollector fieldsCollector = new FieldsCollector();
  public void evaluate(TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<EvaluationResult> handler){
    //todo check it is a valid controlled term
    var values = valuesReporter.getValues();
    var ctFrequency = new HashMap<String, Integer>();
    int filledCtCounts = 0;

    for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
      var path = fieldEntry.getKey();
      var fieldValue = fieldEntry.getValue();
      var valueConstraint = templateReporter.getValueConstraints(path);

      if(!fieldsCollector.isEmptyField(fieldValue) && valueConstraint.isPresent() && valueConstraint.get().isControlledTermValueConstraint()){
        filledCtCounts++;
        var prefLabel = fieldValue.label();
        if(prefLabel.isPresent() && !prefLabel.get().isEmpty()){
          ctFrequency.merge(prefLabel.get(), 1, Integer::sum);
        }
      }
    }

    handler.accept(new EvaluationResult(VOCABULARIES_DISTRIBUTION, FILLED_CONTROLLED_TERMS_COUNT, String.valueOf(filledCtCounts)));
    handler.accept(new EvaluationResult(VOCABULARIES_DISTRIBUTION, CONTROLLED_TERMS_FREQUENCY, ctFrequency.toString()));
  }
}
