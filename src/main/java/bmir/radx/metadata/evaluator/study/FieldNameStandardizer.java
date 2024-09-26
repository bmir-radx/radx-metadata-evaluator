package bmir.radx.metadata.evaluator.study;

import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;

import java.util.HashMap;
import java.util.Map;

public class FieldNameStandardizer {
  public static String standardizeFieldName(String fieldName){
    if (fieldName == null || fieldName.isEmpty()) {
      return fieldName;
    }
    fieldName = fieldName.replaceAll("[?/()]", "");
    fieldName = fieldName.toLowerCase();
    return fieldName;
  }

  public static Map<String, String> getStandardizedMap(TemplateSchemaArtifact templateSchemaArtifact){
    var allFields = templateSchemaArtifact.getFieldKeys();
    Map<String,String> standardizedMap = new HashMap<>();
    for(var field: allFields){
      var standardizedFieldName = standardizeFieldName(field);
      standardizedMap.put(standardizedFieldName, field);
    }
    return standardizedMap;
  }
}
