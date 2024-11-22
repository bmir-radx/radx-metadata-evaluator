package bmir.radx.metadata.evaluator.sharedComponents;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.dataFile.DataFileEvaluator;
import bmir.radx.metadata.evaluator.result.ValidationResult;
import bmir.radx.metadata.evaluator.study.StudyEvaluator;
import bmir.radx.metadata.evaluator.variable.VariableEvaluator;

import java.nio.file.Path;

public interface Evaluator<T extends ValidationResult> {
  EvaluationReport<T> evaluate(Path... filePath);
}
