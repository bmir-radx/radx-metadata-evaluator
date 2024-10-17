package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.EvaluationConstant;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.DUPLICATE_STUDIES;
import static bmir.radx.metadata.evaluator.EvaluationConstant.UNIQUENESS;

@Component
public class StudyUniquenessEvaluator {
  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer){
    Set<String> uniquePHS = new HashSet<>();
    List<Integer> duplicatePHS = new ArrayList<>();


    for(var row: rows){
      String phs = row.studyPHS();
      if(phs == null){
        continue;
      }

      if(!uniquePHS.add(phs)){
        duplicatePHS.add(row.rowNumber());
      }
    }

    int totalStudies = rows.size();
    int uniqueStudies = uniquePHS.size();
    var rate = (double) uniqueStudies / totalStudies * 100;
    String formattedRate = String.format("%.2f%%", rate);
    consumer.accept(new EvaluationResult(UNIQUENESS, formattedRate));
    if(!duplicatePHS.isEmpty()){
      consumer.accept(new EvaluationResult(DUPLICATE_STUDIES, duplicatePHS.toString()));
    }
  }
}
