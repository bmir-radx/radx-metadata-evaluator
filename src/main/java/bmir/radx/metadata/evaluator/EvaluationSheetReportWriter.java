package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.Result;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.*;

import static bmir.radx.metadata.evaluator.util.ChartCreator.createChartImage;
import static bmir.radx.metadata.evaluator.util.ChartCreator.insertChartImageIntoSheet;
import static bmir.radx.metadata.evaluator.util.StringParser.parseToMap;

@Component
public class EvaluationSheetReportWriter {
  public <T extends Result> void writeReports(Workbook workbook, Map<String, List<? extends Result>> reports) {
    for (var entrySet : reports.entrySet()) {
      var sheetName = entrySet.getKey();
      var results = entrySet.getValue();
      var sheet = workbook.createSheet(sheetName);

      // Ensure the results list is not empty to avoid null pointer exceptions
      if (results.isEmpty()) {
        continue;
      }

      // Check if the results are EvaluationResult or ValidationResult and cast accordingly
      if (results.get(0) instanceof EvaluationResult) {
        writeEvaluationReportHeader(sheet);
        writeEvaluationReport((List<EvaluationResult>) results, sheet, sheetName);
      } else if (results.get(0) instanceof SpreadsheetValidationResult || results.get(0) instanceof JsonValidationResult) {
        writeValidationReportHeader(sheet, results.get(0));
        writeValidationResults(results, sheet);
      }
    }
  }

  public void writeEvaluationReportHeader(Sheet sheet) {
    Row headerRow = sheet.createRow(0);
    Cell headerCell0 = headerRow.createCell(0);
    headerCell0.setCellValue("Evaluation Type");
    Cell headerCell1 = headerRow.createCell(1);
    headerCell1.setCellValue("Content");
  }

  public void writeValidationReportHeader(Sheet sheet, Result sampleResult) {
    Row headerRow = sheet.createRow(0);
    if (sampleResult instanceof SpreadsheetValidationResult) {
      createValidationHeader(headerRow, "Row", "Study PHS", "Column", "Value", "Error Type", "Repair Suggestion");
    } else if (sampleResult instanceof JsonValidationResult) {
      createValidationHeader(headerRow, "File Name", "Error Pointer", "Validation Type", "Error Message", "Suggestion");
    }
  }

  private void createValidationHeader(Row headerRow, String... headers) {
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
    }
  }

  public void writeEvaluationReport(List<EvaluationResult> evaluationResults, Sheet sheet, String sheetName) {
    int rowIndex = 1; // Starting row index for data
    int currentRowForChart = 15; // Starting row for the first chart

    for (EvaluationResult r : evaluationResults) {
      Row row = sheet.createRow(rowIndex++);
      String evaluationType = r.getEvaluationConstant().getDisplayName();
      row.createCell(0).setCellValue(evaluationType);
      row.createCell(1).setCellValue(r.getContent());

      // Check if the evaluation type ends with "DISTRIBUTION"
      if (evaluationType.endsWith("Distribution")) {
        // Extract values from the map format string
        var distributionMap = parseToMap(r.getContent());

        // Create the chart using JFreeChart
        var title = evaluationType.replace("_", " ");
        var chartImage = createChartImage(distributionMap, title, sheetName);

        // Insert the chart image into the sheet
        insertChartImageIntoSheet(sheet, chartImage, currentRowForChart, 0);

        // Adjust the row position for the next chart
        currentRowForChart += 25; // Move down 25 rows for the next chart
      }
    }
  }

  private <T extends Result> void writeValidationResults(List<T> validationResults, Sheet sheet) {
    int rowIndex = 1; // Start after the header
    for (T result : validationResults) {
      Row row = sheet.createRow(rowIndex++);

      if (result instanceof SpreadsheetValidationResult spreadsheetResult) {
        row.createCell(0).setCellValue(spreadsheetResult.row());
        row.createCell(1).setCellValue(spreadsheetResult.phsNumber());
        row.createCell(2).setCellValue(spreadsheetResult.column());
        row.createCell(3).setCellValue(spreadsheetResult.value() != null ? spreadsheetResult.value().toString() : "");
        row.createCell(4).setCellValue(spreadsheetResult.errorType());
        row.createCell(5).setCellValue(spreadsheetResult.repairSuggestion());
      } else if (result instanceof JsonValidationResult jsonResult) {
        row.createCell(0).setCellValue(jsonResult.fileName());
        row.createCell(1).setCellValue(jsonResult.pointer());
        row.createCell(2).setCellValue(jsonResult.validationName().name());
        row.createCell(3).setCellValue(jsonResult.errorMessage());
        row.createCell(4).setCellValue(jsonResult.suggestion());
      }
    }
  }
}
