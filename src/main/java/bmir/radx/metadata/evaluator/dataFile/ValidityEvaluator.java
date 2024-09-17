package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationResult;
import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.*;

@Component
public class ValidityEvaluator {
  private final ValidatorFactory validatorFactory;

  public ValidityEvaluator(ValidatorFactory validatorFactory) {
    this.validatorFactory = validatorFactory;
  }


  public void evaluate(String templateString, String instanceString, Consumer<EvaluationResult> consumer) {
    var validator = validatorFactory.createValidator(new LiteralFieldValidators(new HashMap<>()));
    ValidationReport report = null;
    try {
      report = validator.validateInstance(templateString, instanceString);
    } catch (Exception e) {
      throw new RuntimeException("Error validating metadata");
    }
    int errorCount = 0;
      List<String> errors = new ArrayList<>();
      for(var result: report.results()){
        if(result.validationLevel().equals(ValidationLevel.ERROR)){
          errorCount += 1;
          errors.add(result.message());
        }
      }
//      consumer.accept(new EvaluationResult(VALIDATION_ERROR_COUNT, String.valueOf(errorCount)));
      if(errorCount > 0){
        consumer.accept(new EvaluationResult(VALIDATION_ERROR, String.valueOf(errors)));
      } else{
        consumer.accept(new EvaluationResult(VALIDATION_ERROR, "null"));
    }
  }
}
