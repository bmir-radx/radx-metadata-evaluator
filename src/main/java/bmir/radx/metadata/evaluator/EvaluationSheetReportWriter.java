package bmir.radx.metadata.evaluator;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.XSSFChart;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class EvaluationSheetReportWriter {
  public void writeReportHeader(Sheet sheet) {
    Row headerRow = sheet.createRow(0);
    Cell headerCell0 = headerRow.createCell(0);
    headerCell0.setCellValue("Evaluation Type");
    Cell headerCell1 = headerRow.createCell(1);
    headerCell1.setCellValue("Content");
  }

//  public void writeSingleReport(EvaluationReport report, Sheet sheet) {
//    int rowIndex = 1; // Starting row index for data
//    for (EvaluationResult r : report.results()) {
//      Row row = sheet.createRow(rowIndex++);
//      row.createCell(0).setCellValue(r.getEvaluationConstant().name());
//      row.createCell(1).setCellValue(r.getContent());
//    }
//  }

  public void writeSingleReport(EvaluationReport report, Sheet sheet, Workbook workbook, String sheetName) {
    int rowIndex = 1; // Starting row index for data
    int currentRowForChart = 10; // Starting row for the first chart

    for (EvaluationResult r : report.results()) {
      Row row = sheet.createRow(rowIndex++);
      String evaluationType = r.getEvaluationConstant().name();
      row.createCell(0).setCellValue(evaluationType);
      row.createCell(1).setCellValue(r.getContent());

      // Check if the evaluation type ends with "DISTRIBUTION"
      if (evaluationType.endsWith("DISTRIBUTION")) {
        // Extract values from the map format string
        Map<String, Integer> distributionMap = parseDistribution(r.getContent());

        // Create a hidden sheet and write the distribution data there
        String hiddenSheetName = sheetName.replace("Metadata Report", "").trim() + "_" + evaluationType;
        XSSFSheet hiddenSheet = (XSSFSheet) workbook.createSheet(shortenSheetName(hiddenSheetName));
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);

        // Write the distribution data to the hidden sheet
        writeDistributionData(hiddenSheet, distributionMap);

        // Create the chart using the data written to the hidden sheet
        createChart(sheet, hiddenSheet, distributionMap.size(), evaluationType, currentRowForChart, 0);

        // Adjust the row position for the next chart
        currentRowForChart += 15; // Move down 15 rows for the next chart (adjust based on chart size)
      }
    }
  }

  public void writeReports(Workbook workbook, Map<String, EvaluationReport> reports){
    for(var entrySet: reports.entrySet()){
      var sheetName = entrySet.getKey();
      var report = entrySet.getValue();
      var sheet = workbook.createSheet(sheetName);
      writeReportHeader(sheet);
      writeSingleReport(report, sheet, workbook, sheetName);
    }
  }

  // Method to extract values from the distribution string
  private Map<String, Integer> parseDistribution(String distributionText) {
    distributionText = distributionText.replaceAll("[\\{\\}]", ""); // Remove curly braces
    String[] pairs = distributionText.split(",\\s*");
    Map<String, Integer> map = new LinkedHashMap<>();
    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      String key = keyValue[0].trim();
      int value = Integer.parseInt(keyValue[1].trim());
      map.put(key, value);
    }

    // Create a sorted map based on the custom order
    Map<String, Integer> sortedMap = new LinkedHashMap<>();
    List<String> order = Arrays.asList("0%-20%", "20%-40%", "40%-60%", "60%-80%", "80%-100%");
    for (String key : order) {
      if (map.containsKey(key)) {
        sortedMap.put(key, map.get(key));
      }
    }
    return sortedMap;
  }

  // Method to write distribution data into a hidden sheet
  private void writeDistributionData(Sheet hiddenSheet, Map<String, Integer> distributionMap) {
    int rowNum = 0;
    for (Map.Entry<String, Integer> entry : distributionMap.entrySet()) {
      Row row = hiddenSheet.createRow(rowNum++);
      row.createCell(0).setCellValue(entry.getKey());
      row.createCell(1).setCellValue(entry.getValue());
    }
  }

  // Method to create a chart using the data written to the hidden sheet
  private void createChart(Sheet sheet, XSSFSheet hiddenSheet, int dataSize, String title, int startRow, int startColumn) {
    // Define the anchor point for the chart in the sheet
    var drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
    var anchor = drawing.createAnchor(0, 0, 0, 0, startColumn, startRow, startColumn + 7, startRow + 10);

    // Create the chart object
    var chart = drawing.createChart(anchor);

    // Set the chart title
    chart.setTitleText(title);
    chart.setTitleOverlay(false);

    // Create data sources for the chart from the hidden sheet
    XDDFDataSource<String> xs = XDDFDataSourcesFactory.fromStringCellRange(hiddenSheet, new CellRangeAddress(0, dataSize - 1, 0, 0));
    XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(hiddenSheet, new CellRangeAddress(0, dataSize - 1, 1, 1));

    // Create bar chart data
    var data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, chart.createCategoryAxis(AxisPosition.BOTTOM), chart.createValueAxis(AxisPosition.LEFT));
    data.setBarDirection(BarDirection.COL);
    var series = (XDDFBarChartData.Series) data.addSeries(xs, ys);
    series.setTitle(title, null);

    // Plot the chart data
    chart.plot(data);

    // Set legend position
    var legend = chart.getOrAddLegend();
    legend.setPosition(LegendPosition.RIGHT);
  }

  private String shortenSheetName(String sheetName) {
    return sheetName.length() > 31 ? sheetName.substring(0, 31) : sheetName;
  }
}
