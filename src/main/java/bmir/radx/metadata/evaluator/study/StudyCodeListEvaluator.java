package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.SpreadsheetHeaders;
import bmir.radx.metadata.evaluator.SpreadsheetReader;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import bmir.radx.metadata.evaluator.util.StudyHeaderConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Component
public class StudyCodeListEvaluator {
  private final String CODE_LISTS_SHEET_NAME = "Code-Lists";
  private final String NEW_CODE_LISTS_SHEET_NAME = "New Code-List";
  private final int old_value_column = 1;
  private final int new_value_column = 2;
  private final SpreadsheetReader spreadsheetReader;

  public StudyCodeListEvaluator(SpreadsheetReader spreadsheetReader) {
    this.spreadsheetReader = spreadsheetReader;
  }

  public void check(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    //read spreadsheet, get the value sets map <String, set<String>>
//    var codeListValues = spreadsheetReader.readCodeListValues(CODE_LISTS_SHEET_NAME, old_value_column);
    var codeListValues = spreadsheetReader.readCodeListValues(NEW_CODE_LISTS_SHEET_NAME, new_value_column);
    var fields = StudyMetadataRow.class.getDeclaredFields();

    for (var row: rows){
      for(var field: fields){
        field.setAccessible(true);
        try{
          String fieldName = field.getName();
          Object fieldValue = field.get(row);
          if(codeListValues.containsKey(fieldName) && !fieldName.equals("multiCenterStudy") && !fieldName.equals("species")){
            Set<String> validValues = codeListValues.get(fieldName);
            if(fieldValue!= null){
              checkSingleCellValue(fieldName, row.rowNumber(), row.studyPHS(), fieldValue.toString(), validValues, validationSummary);
            }
          }
        } catch (IllegalAccessException e) {
          System.err.println("Unable to access field: " + field.getName());
        }

      }
    }
  }

  private void checkSingleCellValue(String fieldName, int rowNumber, String studyPHS, String value, Set<String> validValues, ValidationSummary<SpreadsheetValidationResult> validationSummary) {
    String[] values = value.split(",");
    for (String v : values) {
      String trimmedValue = v.trim(); // Trim whitespace around the value
      if (!validValues.contains(trimmedValue)) {
        String errorMessage = "The value '" + trimmedValue + "' does not match any allowed value in the set.";
        validationSummary.addInvalidMetadata(studyPHS);
        validationSummary.updateValidationResult(
            new SpreadsheetValidationResult(
                IssueTypeMapping.IssueType.CONTROLLED_VOCABULARY_CONSISTENCY,
                StudyHeaderConverter.convertRowFieldToSpreadsheetHeader(fieldName).getHeaderName(),
                rowNumber,
                studyPHS,
                validValues.toString(),
                trimmedValue,
                errorMessage
            )
        );
      }
    }
  }
}
