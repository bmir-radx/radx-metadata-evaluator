package bmir.radx.metadata.evaluator.variable;

import bmir.radx.metadata.evaluator.EvaluationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.FULL_COMPLETENESS_VARIABLE_RATIO;
import static bmir.radx.metadata.evaluator.EvaluationConstant.INCOMPLETE_VARIABLES_ROWS;

@Component
public class VariableCompletenessEvaluator{
  public void evaluate(List<VariableMetadataRow> rows, Consumer<EvaluationResult> consumer){
    List<Integer> incompleteVariables = new ArrayList<>();
    var nonEmptyRowCount = rows.stream()
        .filter(row -> {
          var isComplete = isCompleteRow(row);
          if (!isComplete) {
            incompleteVariables.add(row.rowNumber());
          }
          return isComplete;
        })
        .count();

    var ratio = ((double)nonEmptyRowCount / rows.size()) * 100;
    consumer.accept(new EvaluationResult(FULL_COMPLETENESS_VARIABLE_RATIO, String.format("%.2f%%",ratio)));
    if(!incompleteVariables.isEmpty()){
      consumer.accept(new EvaluationResult(INCOMPLETE_VARIABLES_ROWS, incompleteVariables.toString()));
    }
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
  private boolean nonEmptyBooleanCell(Boolean value){
    return value!= null;
  }

  private boolean isCompleteRow(VariableMetadataRow row) {
    boolean areCoreFieldsComplete = nonEmptyIntCell(row.fileCount()) &&
        nonEmptyIntCell(row.studyCount()) &&
        nonEmptyListCell(row.dbGaPIDs()) &&
        nonEmptyListCell(row.filesPerStudy()) &&
        nonEmptyListCell(row.radxProgram());

    if (!row.isTier1CDE()) {
      // If isTier1CDE is false, only core fields need to be non-empty
      return areCoreFieldsComplete;
    } else {
      // If isTier1CDE is true, all fields must be non-empty
      return areCoreFieldsComplete &&
          nonEmptyStringCell(row.label()) &&
          nonEmptyStringCell(row.concept()) &&
          nonEmptyStringCell(row.responses()) &&
          nonEmptyStringCell(row.radxGlobalPrompt());
    }
  }
}
