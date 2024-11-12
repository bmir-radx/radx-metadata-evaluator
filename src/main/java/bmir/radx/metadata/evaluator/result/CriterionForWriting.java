package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.EvaluationCriterion;

public class CriterionForWriting {
  private EvaluationCriterion criterion = null;
  private Double passRate = null;
  private Integer failedStudyCount = null;
  private String failedStudies = null;

  public EvaluationCriterion getCriterion() {
    return criterion;
  }

  public void setCriterion(EvaluationCriterion criterion) {
    this.criterion = criterion;
  }

  public Double getPassRate() {
    return passRate;
  }

  public void setPassRate(Double passRate) {
    this.passRate = passRate;
  }

  public Integer getFailedStudyCount() {
    return failedStudyCount;
  }

  public void setFailedStudyCount(Integer failedStudyCount) {
    this.failedStudyCount = failedStudyCount;
  }

  public String getFailedStudies() {
    return failedStudies;
  }

  public void setFailedStudies(String failedStudies) {
    this.failedStudies = failedStudies;
  }
}
