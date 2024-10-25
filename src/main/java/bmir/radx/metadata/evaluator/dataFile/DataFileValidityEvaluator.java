package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.TemplateGetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.renderer.JsonSchemaArtifactRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.VALIDITY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

@Component
public class DataFileValidityEvaluator {
  @Value("${cedar.api.key}")
  private String cedarApiKey;
  @Value("${terminology.server.endpoint}")
  private String tsEndpoint;
  private boolean hasCached = false;
  private final ObjectMapper mapper;
  private final JsonSchemaArtifactRenderer renderer;
  private final ValidatorFactory validatorFactory;
  private final TemplateGetter templateGetter;


  public DataFileValidityEvaluator(ObjectMapper mapper, JsonSchemaArtifactRenderer renderer, ValidatorFactory validatorFactory, TemplateGetter templateGetter) {
    this.mapper = mapper;
    this.renderer = renderer;
    this.templateGetter = templateGetter;
    this.validatorFactory = validatorFactory;
  }

  public void evaluate(Map<Path, TemplateInstanceArtifact> templateInstanceArtifacts,
                                                          Consumer<EvaluationResult> consumer,
                                                          ValidationSummary<JsonValidationResult> validationSummary){
    List<JsonValidationResult> results = validationSummary.getValidationResults();
    Set<String> invalidInstances = validationSummary.getInvalidMetadata();
    var templateString = getTemplateString();
    for(var instance: templateInstanceArtifacts.entrySet()){
      var fileName = instance.getKey().getFileName().toString();
      var instanceString = getInstanceString(instance.getValue());
      if(!isValid(fileName, templateString, instanceString, results)){
        invalidInstances.add(fileName);
      }
    }

    int totalDataFiles = templateInstanceArtifacts.size();
    int invalidDataFiles = invalidInstances.size();
    var rate = (double) (totalDataFiles - invalidDataFiles) / totalDataFiles * 100;
    String formattedRate = String.format("%.2f%%", rate);

    consumer.accept(new EvaluationResult(VALIDITY, VALIDATION_PASS_RATE, formattedRate));
    consumer.accept(new EvaluationResult(VALIDITY, NUMBER_OF_INVALID_DATA_FILE_METADATA, String.valueOf(invalidDataFiles)));
    if(invalidDataFiles > 0){
      consumer.accept(new EvaluationResult(VALIDITY, INVALID_DATA_FILE_METADATA, invalidInstances.toString()));
    }
  }

  public boolean isValid(String fileName, String templateString, String instanceString, List<JsonValidationResult> results) {
//    var terminologyServerHandler = getTerminologyServerHandler();
//    var validator = validatorFactory.createValidator(getLiteralFieldValidatorsComponent(), terminologyServerHandler);
    var validator = validatorFactory.createValidator(new LiteralFieldValidators(new HashMap<>()));
    ValidationReport report;
    try {
//      if(!hasCached){
//        Cache.init(templateString, instanceString, terminologyServerHandler);
//        hasCached = true;
//      }
      report = validator.validateInstance(templateString, instanceString);

    } catch (Exception e) {
      throw new RuntimeException("Error validating data file metadata." + e.getMessage());
    }
    int errorCount = 0;
    List<JsonValidationResult> errors = new ArrayList<>();
    for(var result: report.results()){
      if(result.validationLevel().equals(ValidationLevel.ERROR)){
        errorCount += 1;
        //TODO need to add fileName and suggestion
        errors.add(new JsonValidationResult(
            fileName,
            result.pointer(),
            result.validationName(),
            result.message(),
            ""));
      }
    }

      results.addAll(errors);
    return errorCount <= 0;
  }

//  private TerminologyServerHandler getTerminologyServerHandler() {
//    return new TerminologyServerHandler(cedarApiKey, tsEndpoint);
//  }

  private LiteralFieldValidators getLiteralFieldValidatorsComponent(){
    return new LiteralFieldValidators(new HashMap<>());
  }

  private String getTemplateString(){
    var template = templateGetter.getDataFileTemplate();
    var templateNode = renderer.renderTemplateSchemaArtifact(template);
    try {
      return mapper.writeValueAsString(templateNode);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error processing Data File Template Json");
    }
  }

  private String getInstanceString(TemplateInstanceArtifact instanceArtifact){
    var instanceNode = renderer.renderTemplateInstanceArtifact(instanceArtifact);
    try {
      return mapper.writeValueAsString(instanceNode);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error processing Data File Metadata Instance Json");
    }
  }
}
