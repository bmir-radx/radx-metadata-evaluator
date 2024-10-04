package bmir.radx.metadata.evaluator.sharedComponents;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.dataFile.FieldsCollector;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.study.StudyMetadataRow;
import edu.stanford.bmir.radx.metadata.validator.lib.FieldValues;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.util.List;
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

  public <T extends MetadataRow> URLCount evaluate(T instance, TemplateSchemaArtifact templateSchemaArtifact, List<SpreadsheetValidationResult> validationResults){
    var fields = instance.getClass().getDeclaredFields();
    var urlCount = new URLCount(0,0,0);
    var rowNumber = instance.rowNumber();
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
          checkUrlResolvable(value.toString(), rowNumber, fieldName, urlCount, validationResults);
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Error get value of " + fieldName);
      }
    }
    return urlCount;
  }

  private boolean meetCriteria(FieldValues fieldValues, ValueConstraints valueConstraints){
    return !fieldsCollector.isEmptyField(fieldValues) && (valueConstraints.isControlledTermValueConstraint() || valueConstraints.isLinkValueConstraint());
  }

  private boolean meetCriteria(ValueConstraints valueConstraints){
//    return (valueConstraints.isControlledTermValueConstraint() || valueConstraints.isLinkValueConstraint());
    return (valueConstraints.isLinkValueConstraint());
  }

  public void checkUrlResolvable(String urlString, Integer rowNumber, String fieldName, URLCount urlCount, List<SpreadsheetValidationResult> validationResults){
    String[] urls = urlString.split(",");
    for (String url : urls) {
      url = url.trim();
      if (url.isEmpty()) {
        continue;
      }

      urlCount.incrementTotalURL();
      boolean hostResolvable = isHostResolvable(url);
      boolean urlResolvable = hostResolvable && isUrlResolvable(url);

      if (!hostResolvable || !urlResolvable) {
        urlCount.incrementUnresolvableURL();
        var result = new SpreadsheetValidationResult("Unresolvable URL", fieldName, rowNumber, null, url);
        validationResults.add(result);
      } else {
        urlCount.incrementResolvableURL();
      }
    }
  }
  private boolean isUrlResolvable(String urlString){
    try {
      var url = new URL(urlString);
      var connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(5000);
      connection.setReadTimeout(5000);
      connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
      int responseCode = connection.getResponseCode();
      return (200 <= responseCode && responseCode <= 399);
    } catch (IOException e) {
      return false;
    }
  }

  private boolean isHostResolvable(String urlString){
    try {
      var url = new URL(urlString);
      InetAddress.getAllByName(url.getHost());
      return true;
    } catch (MalformedURLException | UnknownHostException e) {
      return false;
    }
  }
}
