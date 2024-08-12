package bmir.radx.metadata.evaluator;

public class EvaluationResult {
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
