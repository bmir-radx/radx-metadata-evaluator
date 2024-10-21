package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.VALIDITY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

@Component
public class ValidityEvaluator {
  @Value("${cedar.api.key}")
  private String cedarApiKey;
  @Value("${terminology.server.endpoint}")
  private String tsEndpoint;
  private boolean hasCached = false;
  private final ValidatorFactory validatorFactory;


  public ValidityEvaluator(ValidatorFactory validatorFactory) {
    this.validatorFactory = validatorFactory;
  }

  public List<JsonValidationResult> evaluate(String templateString, String instanceString, Consumer<EvaluationResult> consumer) {
//    var terminologyServerHandler = getTerminologyServerHandler();
//    var validator = validatorFactory.createValidator(getLiteralFieldValidatorsComponent(), terminologyServerHandler);
    var validator = validatorFactory.createValidator(new LiteralFieldValidators(new HashMap<>()));
    ValidationReport report = null;
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
          errors.add(new JsonValidationResult("",
              result.pointer(),
              result.validationName(),
              result.message(),
              ""));
        }
      }
//      consumer.accept(new EvaluationResult(VALIDATION_ERROR_COUNT, String.valueOf(errorCount)));
      if(errorCount > 0){
        consumer.accept(new EvaluationResult(VALIDITY, VALIDATION_ERROR, String.valueOf(errors)));
      } else{
        consumer.accept(new EvaluationResult(VALIDITY, VALIDATION_ERROR, "null"));
    }
      return errors;
  }

//  private TerminologyServerHandler getTerminologyServerHandler() {
//    return new TerminologyServerHandler(cedarApiKey, tsEndpoint);
//  }

  private LiteralFieldValidators getLiteralFieldValidatorsComponent(){
    return new LiteralFieldValidators(new HashMap<>());
  }
}
