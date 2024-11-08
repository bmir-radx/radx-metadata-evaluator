package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.result.*;
import org.apache.poi.ss.usermodel.*;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static bmir.radx.metadata.evaluator.statistics.ChartDataFactory.*;
import static bmir.radx.metadata.evaluator.util.ChartCreator.*;
import static bmir.radx.metadata.evaluator.util.RChartCreator.*;

@Component
public class EvaluationSheetReportWriter {
  private int chartColumnNumber = 5;
  private int chartRowNumber = 1;
  private int contentRowNumber = 1;

  public void writeReports(Workbook workbook, Map<String, EvaluationReport<? extends ValidationResult>> reports, Path out) {
    for (var entrySet : reports.entrySet()) {
      var entity = entrySet.getKey();
      var report = entrySet.getValue();
      writeEvaluationReport(entity, report, workbook, out);
      writeIssuesPage(entity, report.validationResults(), workbook);
    }
  }

  private void writeEvaluationReport(String entity, EvaluationReport<? extends ValidationResult> report, Workbook workbook, Path out){
    var evaluationResults = report.evaluationResults();
    if(!evaluationResults.isEmpty()){
      var eSheetName = entity + " Evaluation Report";
      var eSheet = workbook.createSheet(eSheetName);

//      writeEvaluationReportHeader(eSheet);
      writeEvaluationContent(eSheet, eSheetName, report, out);
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

  private void writeEvaluationReportContentHeader(Sheet sheet) {
    Row headerRow = sheet.createRow(contentRowNumber);
    Cell headerCell0 = headerRow.createCell(0);
    headerCell0.setCellValue("Evaluation Criterion");
    Cell headerCell1 = headerRow.createCell(1);
    headerCell1.setCellValue("Metric");
    Cell headerCell2 = headerRow.createCell(2);
    headerCell2.setCellValue("Content");
    contentRowNumber += 1;
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

  private void writeEvaluationContent(Sheet sheet,
                                      String sheetName,
                                      EvaluationReport<? extends ValidationResult> report,
                                      Path rootPath) {
    int rowIndex = 1; // Starting row index for data
    try {
//      startRServe();
      var rConnection = new RConnection();
      var chartPath = getChartPath(rootPath);

      //generate validity pie chart
      getAndInsertRingChart(sheetName, "Metadata Records", chartPath, sheet, rConnection, report);

      //generate issueType pie chart
      getAndInsertRingChart(sheetName, "Issues", chartPath, sheet, rConnection, report);

      //generate completion bar chart
      getAndInsertStackedBarChart(sheetName, chartPath, sheet, rConnection, report);

      //generate filled fields frequency charts
      getAndInsertHistogramChart(sheetName, chartPath, sheet, rConnection, report.evaluationResults());

      //generate controlled term bar chart
      getAndInsertBarChart(sheetName, chartPath, sheet, rConnection, report);

      for (EvaluationResult r : report.evaluationResults()) {
        Row row = sheet.createRow(rowIndex++);
        String metric = r.getEvaluationMetric().getDisplayName();
        row.createCell(0).setCellValue(r.getEvaluationCriteria().getCriterion());
        row.createCell(1).setCellValue(metric);
        //todo rewrite get as string
        row.createCell(2).setCellValue(r.getContentAsString());
      }
      // Close the connection to Rserve
      rConnection.close();
    } catch (RserveException e) {
      throw new RuntimeException("Something wrong with r connection");
    } catch (Exception e) {
      throw new RuntimeException("Unable to generate chart");
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

  private Path getChartPath(Path rootPath){
    try {
      Path chartsPath = rootPath.toRealPath().resolve("charts");
      if (Files.notExists(chartsPath)) {
        Files.createDirectories(chartsPath);
      }
      return chartsPath;
    } catch (IOException e) {
      throw new RuntimeException("Error creating charts directory at" + rootPath);
    }
  }

  private void getAndInsertRingChart(String sheetName, String centerLabel, Path chartPath, Sheet sheet, RConnection rConnection, EvaluationReport<? extends ValidationResult> report) throws Exception {
    var outputPath = chartPath.resolve(sheetName + " " + centerLabel + " Chart.png");
    var validityDistribution = getDataForValidityChart(report);
    var validityPieChart = generateRingChart(rConnection, validityDistribution, outputPath.toString(), centerLabel);
    insertChartImageIntoSheet(sheet, validityPieChart, chartRowNumber, chartColumnNumber);
    chartRowNumber += 25;
  }

  private void getAndInsertStackedBarChart(String sheetName, Path chartPath, Sheet sheet, RConnection rConnection, EvaluationReport<? extends ValidationResult> report) throws REngineException {
    var chartName = sheetName + " Field Completeness Chart.png";
    var outputPath = chartPath.resolve(chartName);
    var completion = getDataForCompletenessChart(report);
    var completionBarChart = generateStackedBarScatter(rConnection, completion, outputPath.toString());
    insertChartImageIntoSheet(sheet, completionBarChart, chartRowNumber, chartColumnNumber);
    chartRowNumber += 25;
  }

  private void getAndInsertBarChart(String sheetName, Path chartPath, Sheet sheet, RConnection rConnection, EvaluationReport<? extends ValidationResult> report) throws Exception {
    var ctDistribution = getDataForControlledTermBarChart(report);
    if(ctDistribution != null){
      var chartName = sheetName + " Controlled Terms Distribution Chart.png";
      var outputPath = chartPath.resolve(chartName);
      var ctDistributionChart = generateCTDistributionChart(rConnection, ctDistribution, outputPath.toString());
      insertChartImageIntoSheet(sheet, ctDistributionChart, chartRowNumber, chartColumnNumber);
      chartRowNumber += 25;
    }
  }

  private void getAndInsertHistogramChart(String sheetName, Path chartPath, Sheet sheet, RConnection rConnection, List<EvaluationResult> evaluationResults) throws Exception {
    for(var result : evaluationResults){
      var metric = result.getEvaluationMetric();
      if(metric.equals(EvaluationMetric.REQUIRED_FIELDS_COMPLETENESS_DISTRIBUTION) ||
          metric.equals(EvaluationMetric.RECOMMENDED_FIELDS_COMPLETENESS_DISTRIBUTION) ||
          metric.equals(EvaluationMetric.OPTIONAL_FIELDS_COMPLETENESS_DISTRIBUTION) ||
          metric.equals(EvaluationMetric.OVERALL_COMPLETENESS_DISTRIBUTION)){
        var data = result.getContentAsMapIntegerInteger();

        var chartFileName = sheetName + metric.getDisplayName().replace("Distribution", "") + ".png";
        var chartFilePath = chartPath.resolve(chartFileName);
        var fieldCategory = metric.getDisplayName().split(" ")[0];
        var chart = generateHistogramChart(rConnection, data, chartFilePath.toString(), fieldCategory);

        insertChartImageIntoSheet(sheet, chart, chartRowNumber, chartColumnNumber);
        chartRowNumber += 25;
      }
    }
  }
}
