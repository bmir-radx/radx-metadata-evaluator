package bmir.radx.metadata.evaluator.sharedComponents;

import bmir.radx.metadata.evaluator.dataFile.FieldsCollector;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.study.StudyMetadataRow;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import bmir.radx.metadata.evaluator.util.URLCount;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import java.util.concurrent.TimeUnit;

import static bmir.radx.metadata.evaluator.study.FieldNameStandardizer.getStandardizedMap;
import static bmir.radx.metadata.evaluator.study.FieldNameStandardizer.standardizeFieldName;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.INVALID_URL;

@Component
public class LinkChecker {
  private static final int CONNECTION_TIMEOUT = 8000; // 8 seconds
  private static final int READ_TIMEOUT = 5000;       // 5 seconds
  private static final int MAX_RETRIES = 10;          // Max retries for 429
  private static final int BASE_BACKOFF = 500;        // Initial backoff time
  private final Cache<String, Boolean> urlStatusCache; // URL cache
  private final FieldsCollector fieldsCollector;

  public LinkChecker(FieldsCollector fieldsCollector) {
    this.fieldsCollector = fieldsCollector;
    this.urlStatusCache = CacheBuilder.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .build();
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
        }
      }
    }

    //note that radx-rad has publication-url in Attribute-Value fields
    for(var avArtifact: avValues){
      var value = avArtifact.fieldValues().jsonLdValue();
      var path = avArtifact.specificationPath();
      if(value.isPresent() && isValidURL(value.get())){
        urlCount.incrementTotalURL();
        updateUnresolvableUrlResult(value.get(), fileName, path, urlCount, validationResults);
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
      boolean resolvable;

      //Use the cache to speed up the URL checking
      var statusMap = urlStatusCache.asMap();
      if(statusMap.containsKey(url)){
        resolvable = statusMap.get(url);
      } else{
        resolvable = isResolvable(url);
        urlStatusCache.put(url, resolvable);
      }

      if (!resolvable) {
        urlCount.incrementUnresolvableURL();
        //Don't add this validation result because spreadsheet validator already handle this type of validation
        var result = new SpreadsheetValidationResult(INVALID_URL, fieldName, rowNumber, phs, null, url);
        validationResults.add(result);
      } else {
        urlCount.incrementResolvableURL();
      }
    }
  }

  public boolean isResolvable(String urlString) {
    try{
      var url = new URL(urlString);
      // Try resolving with HEAD first
//      return checkUrl(url, "HEAD") || checkUrl(url, "GET");
      return checkUrl(url);
    } catch (MalformedURLException e) {
      return false;
    }
  }

  /**
   * This method use GET method only to check if the provided url is resolvable
   */
  private boolean checkUrl(URL url){
    try {
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

  private boolean checkUrl(URL url, String method) {
    var attempt = 0;
    while (attempt <= MAX_RETRIES) {
      int responseCode = -1;
      try {
        var conn = setupConnection(url, method);
        responseCode = conn.getResponseCode();

        // If redirect (3xx), follow the new location
        if (isRedirect(responseCode)) {
          return handleRedirect(url, conn);
        }
        // Handle 429 Too Many Requests with exponential backoff
        if (isTooManyRequests(responseCode)) {
          applyBackoff(attempt);
          attempt++;
          continue;
        }
        // Return true for successful response (2xx)
        return (responseCode >= 200 && responseCode < 300);
      } catch (IOException | InterruptedException e) {
        return false;
      }
    }
    // If all attempts fail, assume it's possibly resolvable
    System.out.println("WARN  All attempts failed; assuming " + url + " might still be resolvable.");
    return true;
  }

  private HttpURLConnection setupConnection(URL url, String method) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    conn.setRequestMethod(method);
    conn.setConnectTimeout(CONNECTION_TIMEOUT);
    conn.setReadTimeout(READ_TIMEOUT);
    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36");
    conn.connect();
    return conn;
  }

  private boolean isRedirect(int responseCode) {
    return responseCode >= 300 && responseCode < 400;
  }

  private boolean handleRedirect(URL url, HttpURLConnection conn) throws IOException {
    String newLocation = conn.getHeaderField("Location");
    if (newLocation != null) {
      URL redirectUrl = new URL(url, newLocation);
      return isResolvable(redirectUrl.toString());
    }
    return false;
  }

  private boolean isTooManyRequests(int responseCode) {
    return responseCode == 429;
  }

  private void applyBackoff(int attempt) throws InterruptedException {
    int backoff = BASE_BACKOFF * attempt;
    TimeUnit.MILLISECONDS.sleep(backoff);
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
    if (!isResolvable(uriString)) {
      urlCount.incrementUnresolvableURL();
      validationResults.add(
          new JsonValidationResult(
              fileName,
              path,
              INVALID_URL,
              "Invalid URL",
              null)
      );
    } else{
      urlCount.incrementResolvableURL();
    }
  }
}
