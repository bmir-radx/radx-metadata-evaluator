package bmir.radx.metadata.evaluator.variable;

import bmir.radx.metadata.evaluator.EvaluationConstant;
import bmir.radx.metadata.evaluator.EvaluationResult;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.FULL_COMPLETENESS_VARIABLE_RATIO;

@Component
public class VariableCompletenessEvaluator{
  public void evaluate(List<VariableMetadataRow> rows, Consumer<EvaluationResult> consumer){
    var nonEmptyRowCount = rows.stream()
        .filter(this::isNonEmptyRow)
        .count();

    var ratio = ((double)nonEmptyRowCount / rows.size()) * 100;
    consumer.accept(new EvaluationResult(FULL_COMPLETENESS_VARIABLE_RATIO, String.valueOf(ratio)));
  }

  private boolean nonEmptyStringCell(String value){
    return value!=null && !value.isEmpty();
  }

  private boolean nonEmptyListCell(List<String> values){
    return !values.isEmpty() && !values.get(0).isEmpty();
  }

  private boolean nonEmptyIntCell(Integer value){
    return value!=null;
  }

  private boolean isNonEmptyRow(VariableMetadataRow row) {
    return nonEmptyStringCell(row.dataVariable()) &&
        nonEmptyIntCell(row.fileCount()) &&
        nonEmptyIntCell(row.studyCount()) &&
        nonEmptyListCell(row.dbGaPIDs()) &&
        nonEmptyListCell(row.filesPerStudy()) &&
        nonEmptyListCell(row.radxProgram()) &&
        nonEmptyStringCell(row.label()) &&
        nonEmptyStringCell(row.concept()) &&
        nonEmptyStringCell(row.responses()) &&
        nonEmptyStringCell(row.radxGlobalPrompt());
  }
}
