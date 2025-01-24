package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.sharedComponents.LinguisticQualityChecker;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import bmir.radx.metadata.evaluator.util.StudyHeaderConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Component
public class StudyLinguisticQualityEvaluator {
  public void check(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    var fields = StudyMetadataRow.class.getDeclaredFields();
    for (var row: rows){
      for(var field: fields){
        field.setAccessible(true);
        try{
          String fieldName = field.getName();
          Object fieldValue = field.get(row);
          if(fieldValue != null){
            String fieldStringValue = fieldValue.toString();
            List<Integer> index = LinguisticQualityChecker.checkExtraSpace(fieldStringValue);
            if(!index.isEmpty()){
              validationSummary.updateValidationResult(
                  new SpreadsheetValidationResult(
                      IssueTypeMapping.IssueType.LINGUISTIC_QUALITY,
                      StudyHeaderConverter.convertRowFieldToSpreadsheetHeader(fieldName).getHeaderName(),
                      row.rowNumber(),
                      row.studyPHS(),
                      "Remove extra space at index " + index.toString(),
                      fieldValue,
                      "Extra space(s) found"
                  )
              );
            }
          }
        } catch (IllegalAccessException e) {
          System.err.println("Unable to access field: " + field.getName());
        }

      }
    }
  }
}
