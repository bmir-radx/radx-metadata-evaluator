package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationResult;
import edu.stanford.bmir.radx.metadata.validator.lib.FieldValues;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.*;

@Component
public class LinkEvaluator {
  private final FieldsCollector fieldsCollector = new FieldsCollector();

  public void evaluate(TemplateReporter templateReporter, TemplateInstanceValuesReporter valuesReporter, Consumer<EvaluationResult> handler){
    var values = valuesReporter.getValues();
    int accessibleUri = 0;
    for(var fieldEntry: values.entrySet()){
      var path = fieldEntry.getKey();
      var valueConstraints = templateReporter.getValueConstraints(path);
      if(valueConstraints.isPresent() && meetCriteria(fieldEntry.getValue(), valueConstraints.get())){
        accessibleUri++;
      }
    }

    //todo radx-rad has publication-url
    handler.accept(new EvaluationResult(ACCESSIBLE_URI_COUNT, String.valueOf(accessibleUri)));
  }

  private boolean meetCriteria(FieldValues fieldValues, ValueConstraints valueConstraints){
    return !fieldsCollector.isEmptyField(fieldValues) && (valueConstraints.isControlledTermValueConstraint() || valueConstraints.isLinkValueConstraint());
  }
}
