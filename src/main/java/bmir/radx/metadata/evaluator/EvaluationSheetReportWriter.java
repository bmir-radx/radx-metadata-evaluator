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
import java.util.stream.Collectors;

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
  private final String METADATA_RECORDS = "Metadata Records";
  private final String EVALUATION_REPORT = "Evaluation Report";
  private MetadataEntity metadataEntity;
  private Workbook workbook;

  public void setWorkbook(MetadataEntity metadataEntity, Workbook workbook) {
    this.workbook = workbook;
    this.metadataEntity = metadataEntity;
  }

  public void writeReports(EvaluationReport<? extends ValidationResult> report, Path out) {
    locateWritingPosition();
    writeEvaluationReport(report, out);
    writeIssuesPage(report.validationResults());

  }

  private void locateWritingPosition(){
    currentChartRowNumber = startChartRowNumber;
    currentChartColumnNumber = startChartColumnNumber;
    currentContentRowNumber = startContentRowNumber;
  }
  private void writeEvaluationReport(EvaluationReport<? extends ValidationResult> report, Path out){
    var evaluationResults = report.evaluationResults();
    if(!evaluationResults.isEmpty()){
      var eSheet = workbook.createSheet(EVALUATION_REPORT);

//      writeEvaluationReportHeader(eSheet);
      writeEvaluationContent(eSheet, report, out);
    }
  }

  /**
   * This method generate single issue page by issue type
   */
  private <T extends ValidationResult> void writeIssuesPage(List<T> validationResults){
    if (!validationResults.isEmpty()) {
      // Group validation results by issue type
      Map<String, List<T>> groupedResults = validationResults.stream()
          .collect(Collectors.groupingBy(result -> {
            if (result instanceof SpreadsheetValidationResult spreadsheetResult) {
              return spreadsheetResult.issueType().getName();
            } else if (result instanceof JsonValidationResult jsonResult) {
              return jsonResult.issueType().getName();
            }
            return "Unknown Issue Type";
          }));

      // Sort issues by the issue level
      Comparator<T> issueComparator = (result1, result2) -> {
        if (result1 instanceof SpreadsheetValidationResult && result2 instanceof SpreadsheetValidationResult) {
          return ((SpreadsheetValidationResult) result1).issueLevel().getLevel()
              .compareTo(((SpreadsheetValidationResult) result2).issueLevel().getLevel());
        } else if (result1 instanceof JsonValidationResult && result2 instanceof JsonValidationResult) {
          return ((JsonValidationResult) result1).issueLevel().getLevel()
              .compareTo(((JsonValidationResult) result2).issueLevel().getLevel());
        }
        return 0; // Default equality for unknown types
      };
      groupedResults.forEach((key, list) -> list.sort(issueComparator));


      // Create a separate sheet for each issue type
      groupedResults.forEach((issueType, results) -> {
        var vSheet = workbook.createSheet(issueType);
        writeIssuePageHeader(vSheet, results.get(0));
        writeIssuePageContent(results, vSheet);
      });
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
      createIssuePageHeader(headerRow, "UUID", "Study PHS", "Row Number", "Column", "Value", "Issue Level", "Repair Suggestion");
    } else if (sampleResult instanceof JsonValidationResult) {
      createIssuePageHeader(headerRow, "UUID", "Study PHS", "File Name", "Error Pointer", "Issue Level", "Error Message", "Suggestion");
    }
  }

  private void createIssuePageHeader(Row headerRow, String... headers) {
    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
    }
  }

  private void writeEvaluationContent(Sheet sheet,
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
//    getAndInsertCharts(sheet, report, rootPath);
  }

  private <T extends ValidationResult> void writeIssuePageContent(List<T> validationResults, Sheet sheet) {
    int rowIndex = 1; // Start after the header
    for (T result : validationResults) {
      Row row = sheet.createRow(rowIndex++);

      if (result instanceof SpreadsheetValidationResult spreadsheetResult) {
        row.createCell(0).setCellValue(spreadsheetResult.uuid());
        row.createCell(1).setCellValue(spreadsheetResult.studyPhs());
        row.createCell(2).setCellValue(spreadsheetResult.row());
        row.createCell(3).setCellValue(spreadsheetResult.column());
        row.createCell(4).setCellValue(spreadsheetResult.value() != null ? spreadsheetResult.value().toString() : "");
        row.createCell(5).setCellValue(spreadsheetResult.issueLevel().getLevel());
        row.createCell(6).setCellValue(spreadsheetResult.repairSuggestion());
      } else if (result instanceof JsonValidationResult jsonResult) {
        row.createCell(0).setCellValue(jsonResult.uuid());
        row.createCell(1).setCellValue(jsonResult.studyPhs());
        row.createCell(2).setCellValue(jsonResult.fileName());
        row.createCell(3).setCellValue(jsonResult.pointer());
        row.createCell(4).setCellValue(jsonResult.issueLevel().getLevel());
        row.createCell(5).setCellValue(jsonResult.errorMessage());
        row.createCell(6).setCellValue(jsonResult.suggestion());
      }
    }
  }

  private void getAndInsertCharts(Sheet sheet,
                                  EvaluationReport<? extends ValidationResult> report,
                                  Path rootPath){
    try {
      var rConnection = new RConnection();
      var chartPath = getChartPath(rootPath);

      //generate validity pie chart
      getAndInsertRingChart(METADATA_RECORDS, chartPath, sheet, rConnection, report);
      //generate issueType pie chart
      getAndInsertRingChart(issues, chartPath, sheet, rConnection, report);
      //generate completion bar chart
      getAndInsertStackedBarChart(chartPath, sheet, rConnection, report);
      //generate filled fields frequency charts
      getAndInsertHistogramChart(chartPath, sheet, rConnection, report.evaluationResults());
      //generate controlled term bar chart
      getAndInsertBarChart(chartPath, sheet, rConnection, report);
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

  private void getAndInsertRingChart(String centerLabel, Path chartPath, Sheet sheet, RConnection rConnection, EvaluationReport<? extends ValidationResult> report) throws Exception {
    var outputPath = chartPath.resolve(metadataEntity.getEntityName() + " " + centerLabel + " Chart.png");
    Map<String, Integer> data = null;
    if(centerLabel.equals(METADATA_RECORDS)){
      data = getDataForValidityChart(report);
    } else{
      data = getDataForIssueTypeChart(report);
    }

    var pieChart = generateRingChart(rConnection, data, outputPath.toString(), centerLabel);
    insertChartImageIntoSheet(sheet, pieChart, currentChartRowNumber, currentChartColumnNumber);
    currentChartRowNumber += 25;
  }

  private void getAndInsertStackedBarChart(Path chartPath, Sheet sheet, RConnection rConnection, EvaluationReport<? extends ValidationResult> report) throws REngineException {
    var chartName = metadataEntity.getEntityName() + " Field Completeness Chart.png";
    var outputPath = chartPath.resolve(chartName);
    var completion = getDataForCompletenessChart(report);
    var completionBarChart = generateStackedBarScatter(rConnection, completion, outputPath.toString());
    insertChartImageIntoSheet(sheet, completionBarChart, currentChartRowNumber, currentChartColumnNumber);
    currentChartRowNumber += 25;
  }

  private void getAndInsertBarChart(Path chartPath, Sheet sheet, RConnection rConnection, EvaluationReport<? extends ValidationResult> report) throws Exception {
    var ctDistribution = getDataForControlledTermBarChart(report);
    if(ctDistribution != null){
      var chartName = metadataEntity.getEntityName() + " Controlled Terms Distribution Chart.png";
      var outputPath = chartPath.resolve(chartName);
      var ctDistributionChart = generateCTDistributionChart(rConnection, ctDistribution, outputPath.toString());
      insertChartImageIntoSheet(sheet, ctDistributionChart, currentChartRowNumber, currentChartColumnNumber);
      currentChartRowNumber += 25;
    }
  }

  private void getAndInsertHistogramChart(Path chartPath, Sheet sheet, RConnection rConnection, List<EvaluationResult> evaluationResults) throws Exception {
    for(var result : evaluationResults){
      var metric = result.getEvaluationMetric();
      if(metric.equals(EvaluationMetric.REQUIRED_FIELDS_COMPLETENESS_DISTRIBUTION) ||
          metric.equals(EvaluationMetric.RECOMMENDED_FIELDS_COMPLETENESS_DISTRIBUTION) ||
          metric.equals(EvaluationMetric.OPTIONAL_FIELDS_COMPLETENESS_DISTRIBUTION) ||
          metric.equals(EvaluationMetric.OVERALL_COMPLETENESS_DISTRIBUTION)){
        var data = result.getContentAsMapIntegerInteger();

        var chartFileName = metadataEntity.getEntityName() + metric.getDisplayName().replace("Distribution", "") + ".png";
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
}
