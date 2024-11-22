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

import static bmir.radx.metadata.evaluator.result.DataFactory.getBasicInfoData;
import static bmir.radx.metadata.evaluator.result.DataFactory.getCriterionData;
import static bmir.radx.metadata.evaluator.statistics.ChartDataFactory.*;
import static bmir.radx.metadata.evaluator.RChartCreator.*;

@Component
public class EvaluationSheetReportWriter {
  private int currentChartColumnNumber;
  private int currentChartRowNumber;
  private int currentContentRowNumber;
  private final int startChartColumnNumber = 0;
  private final int startChartRowNumber = 20;
  private final int startContentRowNumber = 0;
  private final int maxLenth = 10000;
  private final String issues = "Issues";
  private final String metadataRecords = "Metadata Records";
  private Workbook workbook;

  public void writeReports(Map<String, EvaluationReport<? extends ValidationResult>> reports, Path out) {
    for (var entrySet : reports.entrySet()) {
      locateWritingPosition();
      var entity = entrySet.getKey();
      var report = entrySet.getValue();
      writeEvaluationReport(entity, report, out);
      writeIssuesPage(entity, report.validationResults());
    }
  }

  private void locateWritingPosition(){
    currentChartRowNumber = startChartRowNumber;
    currentChartColumnNumber = startChartColumnNumber;
    currentContentRowNumber = startContentRowNumber;
  }
  private void writeEvaluationReport(String entity, EvaluationReport<? extends ValidationResult> report, Path out){
    var evaluationResults = report.evaluationResults();
    if(!evaluationResults.isEmpty()){
      var eSheetName = entity + " Evaluation Report";
      var eSheet = workbook.createSheet(eSheetName);

//      writeEvaluationReportHeader(eSheet);
      writeEvaluationContent(eSheet, eSheetName, report, out);
    }
  }

  private <T extends ValidationResult> void writeIssuesPage(String entity, List<T> validationResults){
    if(!validationResults.isEmpty()){
      var vSheetName = entity + " Validation Report";
      var vSheet = workbook.createSheet(vSheetName);
      writeIssuePageHeader(vSheet, validationResults.get(0));
      writeIssuePageContent(validationResults, vSheet);
    }
  }

  private void writeEvaluationReportContentHeader(Sheet sheet, List<String> headers, CellStyle headerStyle) {
    Row row = sheet.createRow(currentContentRowNumber);
    for (int i = 0; i < headers.size(); i++) {
      Cell cell = row.createCell(i);
      cell.setCellValue(headers.get(i));
      cell.setCellStyle(headerStyle);
    }
    currentContentRowNumber += 1;
  }

  private void writeIssuePageHeader(Sheet sheet, ValidationResult sampleResult) {
    Row headerRow = sheet.createRow(0);
    if (sampleResult instanceof SpreadsheetValidationResult) {
      createIssuePageHeader(headerRow, "Row", "Study PHS", "Column", "Value", "Issue Type", "Repair Suggestion");
    } else if (sampleResult instanceof JsonValidationResult) {
      createIssuePageHeader(headerRow, "File Path", "Error Pointer", "Issue Type", "Error Message", "Suggestion");
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
    //write the Basic Info headers
    var headerStyle = getHeaderStyle();
    var boldStyle = getBoldStyle();
    List<String> basicInfoHeader = Arrays.asList("Basic Info", "Value");
    writeEvaluationReportContentHeader(sheet, basicInfoHeader, headerStyle);

    //write Basic Info data
    var basicInfoData = getBasicInfoData(report.evaluationResults());
    for (var info : basicInfoData) {
      Row row = sheet.createRow(currentContentRowNumber);
      Cell cell = row.createCell(0);
      cell.setCellValue(info.metric().getDisplayName());
      cell.setCellStyle(boldStyle);

      row.createCell(1).setCellValue(info.value().toString());

      currentContentRowNumber++;
    }

    //a blank separate row
    currentContentRowNumber++;

    //write the Criteria headers
    List<String> criteriaHeaders = Arrays.asList("Criteria", "Pass Rate", "Failed Records Count", "Failed Metadata Records");
    writeEvaluationReportContentHeader(sheet, criteriaHeaders, headerStyle);

    //write the criteria data
    var criteriaData = getCriterionData(report.evaluationResults());
    for (var data : criteriaData) {
      Row row = sheet.createRow(currentContentRowNumber);
      Cell cell = row.createCell(0);
      cell.setCellValue(data.getCriterion().getCriterion());
      cell.setCellStyle(boldStyle);

      if(data.getPassRate() != null){
        row.createCell(1).setCellValue(data.getPassRate().toString() + "%");
      }
      if(data.getFailedStudyCount() != null){
        row.createCell(2).setCellValue(data.getFailedStudyCount().toString());
      }
      String failedStudies = data.getFailedStudies();
      if (failedStudies != null && failedStudies.length() > maxLenth) {
        failedStudies = failedStudies.substring(0, maxLenth);
      }
      row.createCell(3).setCellValue(failedStudies);

      currentContentRowNumber++;
    }

    // Auto-size columns for a neat appearance
    for (int i = 0; i < criteriaHeaders.size(); i++) {
      sheet.autoSizeColumn(i);
    }

    //add charts
    getAndInsertCharts(sheet, sheetName, report, rootPath);
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
        row.createCell(4).setCellValue(spreadsheetResult.issueType().getName());
        row.createCell(5).setCellValue(spreadsheetResult.repairSuggestion());
      } else if (result instanceof JsonValidationResult jsonResult) {
        row.createCell(0).setCellValue(jsonResult.fileName());
        row.createCell(1).setCellValue(jsonResult.pointer());
        row.createCell(2).setCellValue(jsonResult.issueType().getName());
        row.createCell(3).setCellValue(jsonResult.errorMessage());
        row.createCell(4).setCellValue(jsonResult.suggestion());
      }
    }
  }

  private void getAndInsertCharts(Sheet sheet,
                                  String sheetName,
                                  EvaluationReport<? extends ValidationResult> report,
                                  Path rootPath){
    try {
      var rConnection = new RConnection();
      var chartPath = getChartPath(rootPath);

      //generate validity pie chart
      getAndInsertRingChart(sheetName, metadataRecords, chartPath, sheet, rConnection, report);
      //generate issueType pie chart
      getAndInsertRingChart(sheetName, issues, chartPath, sheet, rConnection, report);
      //generate completion bar chart
      getAndInsertStackedBarChart(sheetName, chartPath, sheet, rConnection, report);
      //generate filled fields frequency charts
      getAndInsertHistogramChart(sheetName, chartPath, sheet, rConnection, report.evaluationResults());
      //generate controlled term bar chart
      getAndInsertBarChart(sheetName, chartPath, sheet, rConnection, report);
      rConnection.close();
    } catch (RserveException e) {
      throw new RuntimeException("Something wrong with r connection");
    } catch (Exception e) {
      throw new RuntimeException("Unable to generate chart");
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
    Map<String, Integer> data = null;
    if(centerLabel.equals(metadataRecords)){
      data = getDataForValidityChart(report);
    } else{
      data = getDataForIssueTypeChart(report);
    }

    var pieChart = generateRingChart(rConnection, data, outputPath.toString(), centerLabel);
    insertChartImageIntoSheet(sheet, pieChart, currentChartRowNumber, currentChartColumnNumber);
    currentChartRowNumber += 25;
  }

  private void getAndInsertStackedBarChart(String sheetName, Path chartPath, Sheet sheet, RConnection rConnection, EvaluationReport<? extends ValidationResult> report) throws REngineException {
    var chartName = sheetName + " Field Completeness Chart.png";
    var outputPath = chartPath.resolve(chartName);
    var completion = getDataForCompletenessChart(report);
    var completionBarChart = generateStackedBarScatter(rConnection, completion, outputPath.toString());
    insertChartImageIntoSheet(sheet, completionBarChart, currentChartRowNumber, currentChartColumnNumber);
    currentChartRowNumber += 25;
  }

  private void getAndInsertBarChart(String sheetName, Path chartPath, Sheet sheet, RConnection rConnection, EvaluationReport<? extends ValidationResult> report) throws Exception {
    var ctDistribution = getDataForControlledTermBarChart(report);
    if(ctDistribution != null){
      var chartName = sheetName + " Controlled Terms Distribution Chart.png";
      var outputPath = chartPath.resolve(chartName);
      var ctDistributionChart = generateCTDistributionChart(rConnection, ctDistribution, outputPath.toString());
      insertChartImageIntoSheet(sheet, ctDistributionChart, currentChartRowNumber, currentChartColumnNumber);
      currentChartRowNumber += 25;
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

        insertChartImageIntoSheet(sheet, chart, currentChartRowNumber, currentChartColumnNumber);
        currentChartRowNumber += 25;
      }
    }
  }

  private CellStyle getHeaderStyle(){
    var headerStyle = workbook.createCellStyle();
    var headerFont = workbook.createFont();
    headerFont.setBold(true);
    headerStyle.setFont(headerFont);
    headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    return headerStyle;
  }

  private CellStyle getBoldStyle(){
    var boldStyle = workbook.createCellStyle();
    var boldFont = workbook.createFont();
    boldFont.setBold(true);
    boldStyle.setFont(boldFont);
    return boldStyle;
  }

  public void setWorkbook(Workbook workbook) {
    this.workbook = workbook;
  }
}
