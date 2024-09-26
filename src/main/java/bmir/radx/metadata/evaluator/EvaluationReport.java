package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.Result;

import java.util.List;

public record EvaluationReport<T extends Result>(List<EvaluationResult> evaluationResults,
                                                 List<T> validationResults) {
}
