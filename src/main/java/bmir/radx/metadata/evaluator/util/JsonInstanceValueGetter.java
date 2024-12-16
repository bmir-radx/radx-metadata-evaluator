package bmir.radx.metadata.evaluator.util;

import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;

public class JsonInstanceValueGetter {

  public static String getValue(TemplateInstanceArtifact instance, String fieldPath){
    TemplateInstanceValuesReporter valuesReporter = new TemplateInstanceValuesReporter(instance);
    var fieldValue = valuesReporter.getValues().get(fieldPath);
    var value = "";
    value += fieldValue.jsonLdValue().orElse("");
    if(fieldValue.jsonLdId().isPresent()){
      value += "; " + fieldValue.jsonLdId().get().toString();
    }
    return value;
  }

}
