package bmir.radx.metadata.evaluator.dataFile;

import edu.stanford.bmir.radx.metadata.validator.lib.FieldValues;
import org.metadatacenter.artifacts.model.core.ParentSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FieldsCollector {
  public List<String> getAllFields(TemplateSchemaArtifact templateSchemaArtifact){
    return getFieldsFromParentArtifact(templateSchemaArtifact, "");
  }

  public List<String> getFieldsFromParentArtifact(ParentSchemaArtifact parentSchemaArtifact, String parentElementName){
    var fields = parentSchemaArtifact.getFieldNames();
    var fieldPath = fields.stream()
        .map(p-> parentElementName + "/" + p)
        .collect(Collectors.toCollection(ArrayList::new));
    var childElements = parentSchemaArtifact.getElementNames();
    for(var childElement: childElements){
      var childElementSchema = parentSchemaArtifact.getElementSchemaArtifact(childElement);
      fieldPath.addAll(getFieldsFromParentArtifact(childElementSchema, "/" + childElement));
    }
    return fieldPath;
  }

  public boolean isEmptyField(FieldValues fieldValues){
    var jsonLdValue = fieldValues.jsonLdValue();
    var jsonLdId = fieldValues.jsonLdId();
    var jsonLdLabel = fieldValues.label();

    boolean isEmptyId = jsonLdId.map(uri -> uri.toString().isEmpty()).orElse(true);
    boolean isEmptyLabel = jsonLdLabel.map(String::isEmpty).orElse(true);
    boolean isEmptyValue = jsonLdValue.map(String::isEmpty).orElse(true);

    return isEmptyId && isEmptyLabel && isEmptyValue;
  }
}
