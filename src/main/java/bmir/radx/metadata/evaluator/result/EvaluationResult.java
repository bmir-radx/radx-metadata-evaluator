package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.EvaluationMetric;

public class EvaluationResult implements Result{
  private final EvaluationCriterion evaluationCriterion;
  private final EvaluationMetric evaluationMetric;
  private final String content;

  public EvaluationResult(EvaluationCriterion evaluationCriterion, EvaluationMetric evaluationMetric, String content) {
    this.evaluationCriterion = evaluationCriterion;
    this.evaluationMetric = evaluationMetric;
    this.content = content;
  }

  public EvaluationCriterion getEvaluationCriteria() {
    return evaluationCriterion;
  }

  public EvaluationMetric getEvaluationMetric() {
    return evaluationMetric;
  }

  public String getContent() {
    return content;
  }
}
