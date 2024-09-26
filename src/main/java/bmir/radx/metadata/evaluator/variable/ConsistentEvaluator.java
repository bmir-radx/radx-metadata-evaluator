package bmir.radx.metadata.evaluator.variable;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.*;

@Component
public class ConsistentEvaluator {
  public void evaluate(List<VariableMetadataRow> variableMetadataRows, List<AllVariablesRow> allVariablesRows, Consumer<EvaluationResult> consumer){
    evaluateStudyAndFileCounts(variableMetadataRows, consumer);
  }

  private void evaluateStudyAndFileCounts(List<VariableMetadataRow> rows, Consumer<EvaluationResult> consumer){
    int studyInconsistentCount = 0;
    int fileInconsistentCount = 0;
    var studyInconsistentRows = new ArrayList<Integer>();
    var fileInconsistentRows = new ArrayList<Integer>();
    for(var row:rows){
      if(!isConsistent(row.dbGaPIDs(), row.studyCount())){
        studyInconsistentCount++;
        studyInconsistentRows.add(row.rowNumber());
      }
      if(!isConsistent(row.filesPerStudy(), row.fileCount())){
        fileInconsistentCount++;
        fileInconsistentRows.add(row.rowNumber());
      }
    }
    consumer.accept(new EvaluationResult(INCONSISTENT_STUDY_COUNT, String.valueOf(studyInconsistentCount)));
    consumer.accept(new EvaluationResult(INCONSISTENT_FILE_COUNT, String.valueOf(fileInconsistentCount)));
    if(studyInconsistentCount!=0){
      consumer.accept(new EvaluationResult(INCONSISTENT_STUDY_COUNT_ROWS, studyInconsistentRows.toString()));
    }
    if(fileInconsistentCount!=0){
      consumer.accept(new EvaluationResult(INCONSISTENT_FILE_COUNT_ROWS, fileInconsistentRows.toString()));
    }
  }

  private boolean isConsistent(List<String> values, int size){
    return size == values.size();
  }
}
