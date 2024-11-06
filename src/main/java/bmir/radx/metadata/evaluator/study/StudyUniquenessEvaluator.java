package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationMetric.DUPLICATE_STUDIES;
import static bmir.radx.metadata.evaluator.EvaluationMetric.UNIQUENESS;
import static bmir.radx.metadata.evaluator.HeaderName.STUDY_PHS;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.DUPLICATE_RECORD;

@Component
public class StudyUniquenessEvaluator {
  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    Set<String> uniquePHS = new HashSet<>();
    List<Integer> duplicatePHS = new ArrayList<>();


    for(var row: rows){
      String phs = row.studyPHS();
      if(phs == null){
        continue;
      }

      if(!uniquePHS.add(phs)){
        duplicatePHS.add(row.rowNumber());
        validationSummary.addInvalidMetadata(row.studyPHS());
        var result = new SpreadsheetValidationResult(
            DUPLICATE_RECORD,
            STUDY_PHS.getHeaderName(),
            row.rowNumber(),
            phs,
            null,
            phs
        );
        validationSummary.updateValidationResult(result);
      }
    }

    int totalStudies = rows.size();
    int uniqueStudies = uniquePHS.size();
    var rate = (double) uniqueStudies / totalStudies * 100;
    consumer.accept(new EvaluationResult(EvaluationCriterion.UNIQUENESS, UNIQUENESS, rate));
    if(!duplicatePHS.isEmpty()){
      consumer.accept(new EvaluationResult(EvaluationCriterion.UNIQUENESS, DUPLICATE_STUDIES, duplicatePHS));
    }
  }
}
