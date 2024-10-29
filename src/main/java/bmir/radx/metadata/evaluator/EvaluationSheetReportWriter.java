package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.Result;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.util.*;

import static bmir.radx.metadata.evaluator.util.ChartCreator.*;
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
    headerCell0.setCellValue("Evaluation Criterion");
    Cell headerCell1 = headerRow.createCell(1);
    headerCell1.setCellValue("Metric");
    Cell headerCell2 = headerRow.createCell(2);
    headerCell2.setCellValue("Content");
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
    int currentRowForChart = 25; // Starting row for the first chart

    //todo calculate record statistics
//    Map<String, Integer> recordStats = calculateRecordStats(evaluationResults);
//    int totalRecords = recordStats.get("total");
//    int validRecords = recordStats.get("valid");
//    int invalidRecords = recordStats.get("invalid");
//
//    // Generate pie chart for record validity distribution
//    Map<String, Integer> validityDistribution = Map.of(
//            "Valid", validRecords,
//            "Invalid", invalidRecords
//    );
//
//    var pieChartImage = createPieChartImage(validityDistribution, "Record Validity Distribution", sheetName);
//    insertChartImageIntoSheet(sheet, pieChartImage, currentRowForChart, 0);
//    currentRowForChart += 25; // Move down for other charts

    for (EvaluationResult r : evaluationResults) {
      Row row = sheet.createRow(rowIndex++);
      String metric = r.getEvaluationMetric().getDisplayName();
      row.createCell(0).setCellValue(r.getEvaluationCriteria().getCriterion());
      row.createCell(1).setCellValue(metric);
      row.createCell(2).setCellValue(r.getContentAsString());

      // Check if the evaluation type ends with "DISTRIBUTION"
      if (metric.endsWith("Distribution")) {
        // Extract values from the map format string
        var distributionMap = parseToMap(r.getContentAsString());

        // Create the chart using JFreeChart
        var title = metric.replace("_", " ");
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
