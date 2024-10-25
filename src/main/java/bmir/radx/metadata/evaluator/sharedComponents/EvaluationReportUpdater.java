package bmir.radx.metadata.evaluator.sharedComponents;

import bmir.radx.metadata.evaluator.result.EvaluationResult;

import java.util.Map;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.ACCESSIBILITY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.RESOLVABLE_URL_RATE;
import static bmir.radx.metadata.evaluator.EvaluationMetric.URL_COUNT_DISTRIBUTION;

public class EvaluationReportUpdater {
  public static void updateAccessibilityResult(Integer totalURL, Integer totalResolvableURL, Consumer<EvaluationResult> consumer, Map<Integer, Integer> distribution){
    if(totalURL != 0){
      var rate = (double) totalResolvableURL / totalURL * 100;
      String formattedRate = String.format("%.2f%%", rate);
      consumer.accept(new EvaluationResult(ACCESSIBILITY, RESOLVABLE_URL_RATE, formattedRate));
      consumer.accept(new EvaluationResult(ACCESSIBILITY, URL_COUNT_DISTRIBUTION, distribution.toString()));
    }
  }
}
