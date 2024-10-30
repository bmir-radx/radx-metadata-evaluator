package bmir.radx.metadata.evaluator.sharedComponents;

import bmir.radx.metadata.evaluator.dataFile.FieldsCollector;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.study.StudyMetadataRow;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import bmir.radx.metadata.evaluator.util.URLCount;
import edu.stanford.bmir.radx.metadata.validator.lib.FieldValues;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidationName;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.*;
import java.util.List;

import static bmir.radx.metadata.evaluator.study.FieldNameStandardizer.getStandardizedMap;
import static bmir.radx.metadata.evaluator.study.FieldNameStandardizer.standardizeFieldName;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.INVALID_URL;

@Component
public class LinkChecker {
  private final FieldsCollector fieldsCollector;

  public LinkChecker(FieldsCollector fieldsCollector) {
    this.fieldsCollector = fieldsCollector;
  }

  public URLCount checkJson(String fileName,
                            TemplateReporter templateReporter,
                            TemplateInstanceValuesReporter valuesReporter,
                            List<JsonValidationResult> validationResults){
    var values = valuesReporter.getValues();
    var avValues = valuesReporter.getAttributeValueFields();
    URLCount urlCount = new URLCount(0, 0, 0);
    for(var fieldEntry: values.entrySet()){
      var path = fieldEntry.getKey();
      var valueConstraints = templateReporter.getValueConstraints(path);
      if(valueConstraints.isPresent() && meetCriteria(fieldEntry.getValue(), valueConstraints.get())){
        urlCount.incrementTotalURL();
        var uri = fieldEntry.getValue().jsonLdId();
        if(uri.isPresent()){
          var uriString = uri.get().toString();
          updateUnresolvableUrlResult(uriString, fileName, path, urlCount, validationResults);
        } else{
            urlCount.incrementUnresolvableURL();
        }
      }
    }

    //note that radx-rad has publication-url in Attribute-Value fields
    for(var avArtifact: avValues){
      var value = avArtifact.fieldValues().jsonLdValue();
      var path = avArtifact.specificationPath();
      if(value.isPresent() && isValidURL(value.get())){
        urlCount.incrementTotalURL();
        if(isUrlResolvable(value.get())){
          updateUnresolvableUrlResult(value.get(), fileName, path, urlCount, validationResults);
        } else{
          urlCount.incrementUnresolvableURL();
        }
      }
    }

    return urlCount;
  }

  public URLCount checkSpreadsheet(StudyMetadataRow instance,
                                   TemplateSchemaArtifact templateSchemaArtifact,
                                   List<SpreadsheetValidationResult> validationResults){
    var fields = instance.getClass().getDeclaredFields();
    var urlCount = new URLCount(0,0,0);
    var rowNumber = instance.rowNumber();
    var phsNumber = instance.studyPHS();
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
          checkUrlResolvable(value.toString(), rowNumber, phsNumber, fieldName, urlCount, validationResults);
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Error get value of " + fieldName);
      }
    }
    return urlCount;
  }

  private boolean meetCriteria(FieldValues fieldValues, ValueConstraints valueConstraints){
//    return !fieldsCollector.isEmptyField(fieldValues) && (valueConstraints.isControlledTermValueConstraint() || valueConstraints.isLinkValueConstraint());
    return !fieldsCollector.isEmptyField(fieldValues) && valueConstraints.isLinkValueConstraint();
  }

  private boolean meetCriteria(ValueConstraints valueConstraints){
//    return (valueConstraints.isControlledTermValueConstraint() || valueConstraints.isLinkValueConstraint());
    return (valueConstraints.isLinkValueConstraint());
  }

  public void checkUrlResolvable(String urlString, Integer rowNumber, String phs, String fieldName, URLCount urlCount, List<SpreadsheetValidationResult> validationResults){
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
        var result = new SpreadsheetValidationResult(INVALID_URL, fieldName, rowNumber, phs, null, url);
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

  private boolean isValidURL(String urlString){
    try {
      new URL(urlString);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private void updateUnresolvableUrlResult(String uriString, String fileName, String path, URLCount urlCount, List<JsonValidationResult> validationResults) {
    if (isUrlResolvable(uriString)) {
      urlCount.incrementResolvableURL();
      validationResults.add(
          new JsonValidationResult(
              fileName,
              path,
              INVALID_URL,
              "Invalid URL",
              null)
      );
    }
  }
}
