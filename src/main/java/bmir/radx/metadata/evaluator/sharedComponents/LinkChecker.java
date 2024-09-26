package bmir.radx.metadata.evaluator.sharedComponents;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.dataFile.FieldsCollector;
import edu.stanford.bmir.radx.metadata.validator.lib.FieldValues;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.*;
import static bmir.radx.metadata.evaluator.study.FieldNameStandardizer.getStandardizedMap;
import static bmir.radx.metadata.evaluator.study.FieldNameStandardizer.standardizeFieldName;

@Component
public class LinkChecker {
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

  public <T> int evaluate(T instance, TemplateSchemaArtifact templateSchemaArtifact){
    var fields = instance.getClass().getDeclaredFields();
    int accessibleUri = 0;
    for(var field: fields){
      field.setAccessible(true);
      String fieldName = field.getName();
      try {
        var value = field.get(instance);
        var templateReporter = new TemplateReporter(templateSchemaArtifact);
        var standardizedMap = getStandardizedMap(templateSchemaArtifact);
        var fieldPath = "/" + standardizedMap.get(standardizeFieldName(fieldName));
        var valueConstraints = templateReporter.getValueConstraints(fieldPath);
        if(value!= null &&
            !value.equals("") &&
            valueConstraints.isPresent() &&
            meetCriteria(valueConstraints.get())){
          accessibleUri ++;
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Error get value of " + fieldName);
      }
    }
    return accessibleUri;
  }

  private boolean meetCriteria(FieldValues fieldValues, ValueConstraints valueConstraints){
    return !fieldsCollector.isEmptyField(fieldValues) && (valueConstraints.isControlledTermValueConstraint() || valueConstraints.isLinkValueConstraint());
  }

  private boolean meetCriteria(ValueConstraints valueConstraints){
    return (valueConstraints.isControlledTermValueConstraint() || valueConstraints.isLinkValueConstraint());
  }
}
