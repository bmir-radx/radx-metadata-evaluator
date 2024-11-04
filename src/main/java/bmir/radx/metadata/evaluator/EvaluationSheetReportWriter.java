package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.result.*;
import bmir.radx.metadata.evaluator.statistics.CompletenessStatistics;
import bmir.radx.metadata.evaluator.statistics.IssueTypeStatistics;
import bmir.radx.metadata.evaluator.statistics.RecordStatistics;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

import static bmir.radx.metadata.evaluator.statistics.ChartDataFactory.*;
import static bmir.radx.metadata.evaluator.statistics.StatisticsCalculator.*;
import static bmir.radx.metadata.evaluator.util.ChartCreator.*;
import static bmir.radx.metadata.evaluator.util.StringParser.parseToMap;

@Component
public class EvaluationSheetReportWriter {
  public void writeReports(Workbook workbook, Map<String, EvaluationReport<? extends ValidationResult>> reports) {
    for (var entrySet : reports.entrySet()) {
      var entity = entrySet.getKey();
      var report = entrySet.getValue();
      writeEvaluationReport(entity, report, workbook);
      writeIssuesPage(entity, report.validationResults(), workbook);
    }
  }

  private void writeEvaluationReport(String entity, EvaluationReport<? extends ValidationResult> report, Workbook workbook){
    var evaluationResults = report.evaluationResults();
    if(!evaluationResults.isEmpty()){
      var eSheetName = entity + " Evaluation Report";
      var eSheet = workbook.createSheet(eSheetName);
      var recordStats = calculateRecordStats(report);
      var issueTypeStats = calculateIssueTypeStatistics(report);
      var completionStats = calculateCompletenessStatistics(report);
      writeEvaluationReportHeader(eSheet);
      writeEvaluationContent(evaluationResults, eSheet, eSheetName, recordStats, issueTypeStats, completionStats);
    }
  }

  private <T extends ValidationResult> void writeIssuesPage(String entity, List<T> validationResults, Workbook workbook){
    if(!validationResults.isEmpty()){
      var vSheetName = entity + " Validation Report";
      var vSheet = workbook.createSheet(vSheetName);
      writeIssuePageHeader(vSheet, validationResults.get(0));
      writeIssuePageContent(validationResults, vSheet);
    }
  }

  private void writeEvaluationReportHeader(Sheet sheet) {
    Row headerRow = sheet.createRow(0);
    Cell headerCell0 = headerRow.createCell(0);
    headerCell0.setCellValue("Evaluation Criterion");
    Cell headerCell1 = headerRow.createCell(1);
    headerCell1.setCellValue("Metric");
    Cell headerCell2 = headerRow.createCell(2);
    headerCell2.setCellValue("Content");
  }

  private void writeIssuePageHeader(Sheet sheet, ValidationResult sampleResult) {
    Row headerRow = sheet.createRow(0);
    if (sampleResult instanceof SpreadsheetValidationResult) {
      createIssuePageHeader(headerRow, "Row", "Study PHS", "Column", "Value", "Issue Type", "Repair Suggestion");
    } else if (sampleResult instanceof JsonValidationResult) {
      createIssuePageHeader(headerRow, "File Name", "Error Pointer", "Issue Type", "Error Message", "Suggestion");
    }
  }

  private void createIssuePageHeader(Row headerRow, String... headers) {
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
    }
  }

  private void writeEvaluationContent(List<EvaluationResult> evaluationResults,
                                      Sheet sheet,
                                      String sheetName,
                                      RecordStatistics recordStatistics,
                                      List<IssueTypeStatistics> issueTypeStatistics,
                                      CompletenessStatistics completenessStatistics) {
    int rowIndex = 1; // Starting row index for data
    int currentRowForChart = 25; // Starting row for the first chart

    //generate validity pie chart
    var validityDistribution = getValidityDistribution(recordStatistics);
    var validityPieChart = createPieChartImage(validityDistribution, "Records by Validity");
    insertChartImageIntoSheet(sheet, validityPieChart, currentRowForChart, 0);
    currentRowForChart += 25; // Move down for other charts

    //generate issueType pie chart
    var issueTypeDistribution = getIssueTypeDistribution(issueTypeStatistics);
    var issueTypePieChart = createPieChartImage(issueTypeDistribution, "Records by Issue Type");
    insertChartImageIntoSheet(sheet, issueTypePieChart, currentRowForChart, 0);
    currentRowForChart += 25;

    //generate completion bar chart
    var completion = getCompletenessDistribution(completenessStatistics);
    var completionBarChart = createCompletionBarChart(completion, "Fields Completeness");
    insertChartImageIntoSheet(sheet, completionBarChart, currentRowForChart, 0);
    currentRowForChart += 25;

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
        var chartImage = createDistributionChart(distributionMap, title, sheetName);

        // Insert the chart image into the sheet
        insertChartImageIntoSheet(sheet, chartImage, currentRowForChart, 0);

        // Adjust the row position for the next chart
        currentRowForChart += 25; // Move down 25 rows for the next chart
      }
    }
  }

  private <T extends ValidationResult> void writeIssuePageContent(List<T> validationResults, Sheet sheet) {
    int rowIndex = 1; // Start after the header
    for (T result : validationResults) {
      Row row = sheet.createRow(rowIndex++);

      if (result instanceof SpreadsheetValidationResult spreadsheetResult) {
        row.createCell(0).setCellValue(spreadsheetResult.row());
        row.createCell(1).setCellValue(spreadsheetResult.phsNumber());
        row.createCell(2).setCellValue(spreadsheetResult.column());
        row.createCell(3).setCellValue(spreadsheetResult.value() != null ? spreadsheetResult.value().toString() : "");
        row.createCell(4).setCellValue(spreadsheetResult.issueType().name());
        row.createCell(5).setCellValue(spreadsheetResult.repairSuggestion());
      } else if (result instanceof JsonValidationResult jsonResult) {
        row.createCell(0).setCellValue(jsonResult.fileName());
        row.createCell(1).setCellValue(jsonResult.pointer());
        row.createCell(2).setCellValue(jsonResult.issueType().name());
        row.createCell(3).setCellValue(jsonResult.errorMessage());
        row.createCell(4).setCellValue(jsonResult.suggestion());
      }
    }
  }
}
