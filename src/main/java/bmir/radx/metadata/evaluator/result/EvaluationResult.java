package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.EvaluationConstant;

public class EvaluationResult implements Result{
  private EvaluationConstant evaluationConstant;
  private String content;

  public EvaluationResult(EvaluationConstant evaluationConstant, String content) {
    this.evaluationConstant = evaluationConstant;
    this.content = content;
  }

  public EvaluationConstant getEvaluationConstant() {
    return evaluationConstant;
  }

  public String getContent() {
    return content;
  }
}
