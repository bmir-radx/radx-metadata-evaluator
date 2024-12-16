package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.sharedComponents.GrammarChecker;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

@Component
public class StudyGrammarChecker {
  private final GrammarChecker grammarChecker;

  public StudyGrammarChecker(GrammarChecker grammarChecker) {
    this.grammarChecker = grammarChecker;
  }

  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    for(var row: rows){
      evaluateSingleStudyMetadata(row);
    }
  }

  private void evaluateSingleStudyMetadata(StudyMetadataRow row){
    var clazz = row.getClass();
    var phs = row.studyPHS();
    for(var field: clazz.getDeclaredFields()){
      field.setAccessible(true);
      try{
        Object value = field.get(row);
        if (value instanceof String) {
          grammarChecker.check((String) value, phs, field.getName());
        }
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

}
