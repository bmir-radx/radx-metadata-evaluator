package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.EvaluationMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class EvaluationResult implements Result{
  private final EvaluationCriterion evaluationCriterion;
  private final EvaluationMetric evaluationMetric;
  private final Object content;

  public EvaluationResult(EvaluationCriterion evaluationCriterion, EvaluationMetric evaluationMetric, Object content) {
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

  public Object getContent() {
    return content;
  }

  public Integer getContentAsInteger() {
    return content instanceof Integer ? (Integer) content : null;
  }

  public Map<String, Integer> getContentAsMapStringInteger() {
    return content instanceof Map<?, ?> ? (Map<String, Integer>) content : null;
  }

  public Map<Integer, Integer> getContentAsMapIntegerInteger() {
    return content instanceof Map<?, ?> ? (Map<Integer, Integer>) content : null;
  }

  public Double getContentAsDouble() {
    if (content instanceof Double) {
      return Math.round((Double) content * 100.0) / 100.0;
    }
    return null;
  }

  public List<String> getContentAsListString() {
    if (content instanceof List<?>) {
      return (List<String>) content;
    } else if (content instanceof Set<?>) {
      return new ArrayList<>((Set<String>) content);
    }
    return null;
  }

  public String getContentAsString() {
    if (content instanceof Double) {
      return String.format("%.2f", (Double) content);
    }
    return content != null ? content.toString() : null;
  }
}
