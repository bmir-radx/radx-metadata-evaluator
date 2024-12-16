package bmir.radx.metadata.evaluator.study;

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

import static bmir.radx.metadata.evaluator.EvaluationCriterion.UNIQUENESS;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.HeaderName.STUDY_PHS;

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
            IssueTypeMapping.IssueType.UNIQUENESS,
            STUDY_PHS.getHeaderName(),
            row.rowNumber(),
            phs,
            null,
            phs,
            "Duplicate Study PHS " + phs

        );
        validationSummary.updateValidationResult(result);
      }
    }

    int totalStudies = rows.size();
    int uniqueStudies = uniquePHS.size();
    var rate = (double) uniqueStudies / totalStudies * 100;
    consumer.accept(new EvaluationResult(UNIQUENESS, UNIQUENESS_RATE, rate));
    consumer.accept(new EvaluationResult(UNIQUENESS, NUMBER_OF_DUPLICATE_RECORDS, duplicatePHS.size()));
    if(!duplicatePHS.isEmpty()){
      consumer.accept(new EvaluationResult(UNIQUENESS, DUPLICATE_RECORDS, duplicatePHS));
    }
  }
}
