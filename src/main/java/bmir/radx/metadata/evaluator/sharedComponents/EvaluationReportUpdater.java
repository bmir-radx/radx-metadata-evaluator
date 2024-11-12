package bmir.radx.metadata.evaluator.sharedComponents;

import bmir.radx.metadata.evaluator.result.EvaluationResult;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.ACCESSIBILITY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

public class EvaluationReportUpdater {
  public static void updateAccessibilityResult(Integer totalURL, Integer totalResolvableURL, Consumer<EvaluationResult> consumer, Map<Integer, Integer> distribution, Set<String> inaccessibleRecords){
    if(totalURL != 0){
      var rate = (double) totalResolvableURL / totalURL * 100;
      consumer.accept(new EvaluationResult(ACCESSIBILITY, RESOLVABLE_URL_RATE, rate));
      consumer.accept(new EvaluationResult(ACCESSIBILITY, NUMBER_OF_INACCESSIBLE_RECORDS, inaccessibleRecords.size()));
      consumer.accept(new EvaluationResult(ACCESSIBILITY, INACCESSIBLE_RECORDS, inaccessibleRecords));

//      consumer.accept(new EvaluationResult(ACCESSIBILITY, URL_COUNT_DISTRIBUTION, distribution));
    } else{
      consumer.accept(new EvaluationResult(ACCESSIBILITY, RESOLVABLE_URL_RATE, 100.0));
      consumer.accept(new EvaluationResult(ACCESSIBILITY, NUMBER_OF_INACCESSIBLE_RECORDS, 0));
      consumer.accept(new EvaluationResult(ACCESSIBILITY, INACCESSIBLE_RECORDS, null));
    }
  }
}
