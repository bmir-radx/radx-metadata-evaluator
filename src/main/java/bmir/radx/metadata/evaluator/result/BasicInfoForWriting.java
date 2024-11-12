package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.EvaluationMetric;

public record BasicInfoForWriting(EvaluationMetric metric,
                                  Integer value) {
}
