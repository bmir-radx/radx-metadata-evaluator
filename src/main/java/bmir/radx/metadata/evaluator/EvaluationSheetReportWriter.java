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
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTDLbls;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EvaluationSheetReportWriter {
  public void writeReportHeader(Sheet sheet) {
    Row headerRow = sheet.createRow(0);
    Cell headerCell0 = headerRow.createCell(0);
    headerCell0.setCellValue("Evaluation Type");
    Cell headerCell1 = headerRow.createCell(1);
    headerCell1.setCellValue("Content");
  }

  public void writeSingleReport(EvaluationReport report, Sheet sheet, Workbook workbook, String sheetName) {
    int rowIndex = 1; // Starting row index for data
    int currentRowForChart = 10; // Starting row for the first chart

    for (EvaluationResult r : report.results()) {
      Row row = sheet.createRow(rowIndex++);
      String evaluationType = r.getEvaluationConstant().getDisplayName();
      row.createCell(0).setCellValue(evaluationType);
      row.createCell(1).setCellValue(r.getContent());

      // Check if the evaluation type ends with "DISTRIBUTION"
      if (evaluationType.endsWith("Distribution")) {
        // Extract values from the map format string
        var distributionMap = parseDistribution(r.getContent());

        // Create a hidden sheet and write the distribution data there
        String hiddenSheetName = sheetName.replace("Metadata Report", "").trim() + "_" + evaluationType;
        XSSFSheet hiddenSheet = (XSSFSheet) workbook.createSheet(shortenSheetName(hiddenSheetName));
        workbook.setSheetHidden(workbook.getSheetIndex(hiddenSheet), true);

        // Write the distribution data to the hidden sheet
        writeDistributionData(hiddenSheet, distributionMap);

        // Create the chart using the data written to the hidden sheet
        var title = evaluationType.replace("_", " ");
        createChart(sheet, hiddenSheet, distributionMap.size(), title, currentRowForChart, 0);

        // Adjust the row position for the next chart
        currentRowForChart += 15; // Move down 15 rows for the next chart
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
  private Map<Integer, Integer> parseDistribution(String distributionText) {
    distributionText = distributionText.replaceAll("[\\{\\}]", "");
    String[] pairs = distributionText.split(",\\s*");
    Map<Integer, Integer> map = new TreeMap<>();
    int maxKey = 0;
    for (String pair : pairs) {
      String[] keyValue = pair.split("=");
      int key = Integer.parseInt(keyValue[0].trim());
      int value = Integer.parseInt(keyValue[1].trim());
      map.put(key, value);
      if (key > maxKey) {
        maxKey = key;
      }
    }
    // Ensure all keys from 0 to maxKey are present, with default value 0 if missing
    for (int i = 0; i <= maxKey; i++) {
      map.putIfAbsent(i, 0);
    }

    return map;
  }

  // Method to write distribution data into a hidden sheet
  private void writeDistributionData(Sheet hiddenSheet, Map<Integer, Integer> distributionMap) {
    int rowNum = 0;
    for (Map.Entry<Integer, Integer> entry : distributionMap.entrySet()) {
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
    XDDFDataSource<Double> xs = XDDFDataSourcesFactory.fromNumericCellRange(hiddenSheet, new CellRangeAddress(0, dataSize - 1, 0, 0));
    XDDFNumericalDataSource<Double> ys = XDDFDataSourcesFactory.fromNumericCellRange(hiddenSheet, new CellRangeAddress(0, dataSize - 1, 1, 1));

    // Check if data sources have correct range and types
    if (xs == null || ys == null || dataSize <= 0) {
      // Log the issue or handle it appropriately
      System.out.println("No data available for chart generation.");
      return;
    }

    // Create bar chart data
    var data = (XDDFBarChartData) chart.createData(ChartTypes.BAR, chart.createCategoryAxis(AxisPosition.BOTTOM), chart.createValueAxis(AxisPosition.LEFT));
    data.setBarDirection(BarDirection.COL);
    var series = (XDDFBarChartData.Series) data.addSeries(xs, ys);
    series.setTitle(title, null);

    // Set axis titles
    data.getCategoryAxis().setTitle("Filled Field Number");
    data.getValueAxes().get(0).setTitle("File Number");

    // Set axis titles
    var bottomAxis = (XDDFCategoryAxis) data.getCategoryAxis();
    bottomAxis.setTitle("Filled Field Number");
    var leftAxis = data.getValueAxes().get(0);
    leftAxis.setTitle("File Number");

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
