package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.ValidationResult;

import java.util.List;

public record EvaluationReport<T extends ValidationResult>(List<EvaluationResult> evaluationResults,
                                                           List<T> validationResults) {
}
