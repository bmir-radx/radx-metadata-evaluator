package bmir.radx.metadata.evaluator;

public enum EvaluationCriterion {
  BASIC_INFO("Basic Info"),
  COMPLETENESS("Completeness"),
  CONSISTENCY("Consistency"),
  ACCURACY("Accuracy"),
  VALIDITY("Validity"),
  ACCESSIBILITY("Accessibility"),
  VOCABULARIES_DISTRIBUTION("Vocabularies Distribution"),
  UNIQUENESS("Uniqueness");

  private final String criterion;

  EvaluationCriterion(String criterion) {
    this.criterion = criterion;
  }

  public String getCriterion() {
    return criterion;
  }
}
