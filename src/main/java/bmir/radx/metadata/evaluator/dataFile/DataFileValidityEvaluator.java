package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.JsonInstanceValueGetter;
import bmir.radx.metadata.evaluator.util.StudyPhsGetter;
import bmir.radx.metadata.evaluator.util.TemplateGetter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import edu.stanford.bmir.radx.metadata.validator.lib.*;
import edu.stanford.bmir.radx.metadata.validator.lib.thirdPartyValidators.TerminologyServerHandler;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ControlledTermValueConstraints;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;
import org.metadatacenter.artifacts.model.renderer.JsonArtifactRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.VALIDITY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.getIssueType;

@Component
public class DataFileValidityEvaluator {
  @Value("${cedar.api.key}")
  private String cedarApiKey;
  @Value("${terminology.server.endpoint}")
  private String tsEndpoint;
  private boolean hasCached = false;
  private final ObjectMapper mapper;
  private final JsonArtifactRenderer renderer = new JsonArtifactRenderer();
  private final ValidatorFactory validatorFactory;
  private final TemplateGetter templateGetter;
  private final StudyPhsGetter studyPhsGetter;
  private final Cache<ControlledTermValueConstraints, Map<String, String>> cache;

  public DataFileValidityEvaluator(ObjectMapper mapper, ValidatorFactory validatorFactory, TemplateGetter templateGetter, StudyPhsGetter studyPhsGetter) {
    this.mapper = mapper;
    this.templateGetter = templateGetter;
    this.validatorFactory = validatorFactory;
    this.studyPhsGetter = studyPhsGetter;
    this.cache = Caffeine.newBuilder()
        .expireAfterWrite(10, TimeUnit.MINUTES) // Cache expiration
        .maximumSize(100) // Maximum cache size
        .build();
  }

  public void evaluate(Map<Path, TemplateInstanceArtifact> templateInstanceArtifacts,
                                                          Consumer<EvaluationResult> consumer,
                                                          ValidationSummary<JsonValidationResult> validationSummary){
    List<JsonValidationResult> results = validationSummary.getValidationResults();
    Set<String> invalidInstances = validationSummary.getInvalidMetadata();
    var templateString = templateGetter.getDataFileTemplateString();
    for(var instance: templateInstanceArtifacts.entrySet()){
      var path  = instance.getKey();
      var fileName = path.getFileName().toString();
      var studyPhs = studyPhsGetter.getCleanStudyPhs(instance.getValue());
      String instanceString = null;
      try {
        instanceString = Files.readString(path);
      } catch (IOException e) {
        throw new RuntimeException("Unable to read file " + path);
      }
      if(!isValid(studyPhs, fileName, templateString, instanceString, instance.getValue(), results)){
        invalidInstances.add(fileName);
      }
    }

    int totalDataFiles = templateInstanceArtifacts.size();
    int invalidDataFiles = invalidInstances.size();
    var rate = (double) (totalDataFiles - invalidDataFiles) / totalDataFiles * 100;

    consumer.accept(new EvaluationResult(VALIDITY, VALIDATION_PASS_RATE, rate));
    consumer.accept(new EvaluationResult(VALIDITY, NUMBER_OF_INVALID_RECORDS, invalidDataFiles));
    consumer.accept(new EvaluationResult(VALIDITY, INVALID_DATA_FILE_METADATA, invalidInstances));

  }

  public boolean isValid(String studyPhs, String fileName, String templateString, String instanceString, TemplateInstanceArtifact instanceArtifact, List<JsonValidationResult> results) {
    var validator = validatorFactory.createValidator(getLiteralFieldValidatorsComponent(), getTerminologyServerHandler());
//    var validator = validatorFactory.createValidator(new LiteralFieldValidators(new HashMap<>()));
    ValidationReport report;
    try {
//      if(!hasCached){
//        Cache.init(templateString, instanceString, terminologyServerHandler);
//        hasCached = true;
//      }
      report = validator.validateInstance(templateString, instanceString, cache);

    } catch (Exception e) {
      throw new RuntimeException("Error validating data file metadata." + e.getMessage());
    }
    int errorCount = 0;
    List<JsonValidationResult> errors = new ArrayList<>();
    for(var result: report.results()){
      if(result.validationLevel().equals(ValidationLevel.ERROR)){
        errorCount += 1;
        var pointer = result.pointer().replace("\"", "").substring(1);
        var value = JsonInstanceValueGetter.getValue(instanceArtifact, pointer);
        errors.add(new JsonValidationResult(
            studyPhs,
            fileName,
            pointer,
            getIssueType(result.validationName()),
            result.message(),
            "",
            value));
      }
    }

      results.addAll(errors);
    return errorCount <= 0;
  }

  private TerminologyServerHandler getTerminologyServerHandler() {
    return new TerminologyServerHandler(cedarApiKey, tsEndpoint);
  }

  private LiteralFieldValidators getLiteralFieldValidatorsComponent(){
    return new LiteralFieldValidators(new HashMap<>());
  }
}
