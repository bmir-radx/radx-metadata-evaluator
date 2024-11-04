package bmir.radx.metadata.evaluator.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.renderer.category.ScatterRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;

public class ChartCreator {
  public static <K> BufferedImage createDistributionChart(Map<K, Integer> distributionMap, String title, String sheetName) {
    var dataset = new DefaultCategoryDataset();

    // Determine the type of keys in the map
    if (!distributionMap.isEmpty()) {
      Object key = distributionMap.keySet().iterator().next();
      if (key instanceof Integer) {
        // Handle Map<Integer, Integer>
        for (Map.Entry<K, Integer> entry : distributionMap.entrySet()) {
          dataset.addValue(entry.getValue(), "Frequency", (Integer) entry.getKey());
        }
      } else if (key instanceof String) {
        // Handle Map<String, Integer>
        for (Map.Entry<K, Integer> entry : distributionMap.entrySet()) {
          dataset.addValue(entry.getValue(), "Frequency", (String) entry.getKey());
        }
      }
    }

    return createDistributionChartFromDataset(dataset, title, sheetName);
  }

  // Common method to generate chart from dataset
  private static BufferedImage createDistributionChartFromDataset(DefaultCategoryDataset dataset, String title, String sheetName) {
    var yAxisLabel = sheetName.replace("Evaluation Report", "").trim();
    if (yAxisLabel.equalsIgnoreCase("Study")) {
      yAxisLabel = "Studies";
    } else {
      yAxisLabel = yAxisLabel + "s";
    }
    var xAxisLabel = title.replace(" Distribution", "");
    if (xAxisLabel.endsWith("Completeness")) {
      xAxisLabel = "Filled " + xAxisLabel.replace("Completeness", "");
    }

    if(title.contains("Controlled Terms")){
      title = "Usage of Controlled Terms";
      yAxisLabel = "Count";
    }

    // Create chart
    var chart = ChartFactory.createBarChart(
        title, // Chart title
        xAxisLabel, // X-Axis Label
        yAxisLabel, // Y-Axis Label
        dataset, // Dataset
        PlotOrientation.VERTICAL,
        false, // Include legend
        true, // Tooltips
        false // URLs
    );

    // Set the overall background color of the chart
    chart.setBackgroundPaint(Color.WHITE);

    // Get the plot object (the plot is the area where the data is drawn)
    var plot = chart.getCategoryPlot();

    // Set the background color of the plot (inside the axes)
    plot.setBackgroundPaint(Color.WHITE);

    // Set the color of the gridlines
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
    plot.setDomainGridlinesVisible(true);
    plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

    // Customize the renderer (the actual bars)
    var renderer = (BarRenderer) plot.getRenderer();

    // Disable shadows to keep it 2D
    renderer.setBarPainter(new StandardBarPainter());

    // Set the bar color (apply to series 0)
    renderer.setSeriesPaint(0, Color.BLUE);

    // **Add labels on top of each bar**

    // Enable item labels for the renderer
    renderer.setDefaultItemLabelsVisible(true);
    renderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator());

    // Position the labels on top of the bars
    renderer.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
        ItemLabelAnchor.OUTSIDE12, TextAnchor.BASELINE_CENTER));

    // Adjust the label font, color, etc.
    renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 12));
    renderer.setDefaultItemLabelPaint(Color.BLACK);

    // Rotate the X-axis labels slightly for better readability
    var xAxis = plot.getDomainAxis();
    xAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

    // Generate image
    return chart.createBufferedImage(600, 400);
  }

  // Method to insert chart image into the sheet
  public static void insertChartImageIntoSheet(Sheet sheet, BufferedImage chartImage, int rowNumber, int columnNumber) {
    try {
      // Convert BufferedImage to byte array
      var chartOut = new ByteArrayOutputStream();
      javax.imageio.ImageIO.write(chartImage, "png", chartOut);
      chartOut.close();
      byte[] imageBytes = chartOut.toByteArray();

      // Add picture to workbook
      int pictureIdx = sheet.getWorkbook().addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG);

      // Create drawing patriarch
      Drawing<?> drawing = sheet.createDrawingPatriarch();

      // Create anchor
      ClientAnchor anchor = sheet.getWorkbook().getCreationHelper().createClientAnchor();
      anchor.setCol1(columnNumber);
      anchor.setRow1(rowNumber);
      anchor.setCol2(columnNumber + 10); // Adjust as needed
      anchor.setRow2(rowNumber + 20); // Adjust as needed

      // Create picture
      Picture pict = drawing.createPicture(anchor, pictureIdx);

      // Resize picture to fit the anchor
      pict.resize();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static BufferedImage createPieChartImage(Map<String, Integer> validityDistribution, String chartTitle) {
    // Create the dataset for the pie chart
    DefaultPieDataset dataset = new DefaultPieDataset();
    validityDistribution.forEach(dataset::setValue);

    // Create the pie chart
    JFreeChart pieChart = ChartFactory.createPieChart(
        chartTitle,          // Chart title
        dataset,             // Dataset
        false,               // Include legend
        true,
        false
    );

    // Customize the chart to make it look like a ring chart
    var plot = (PiePlot) pieChart.getPlot();
    plot.setSectionOutlinesVisible(false);
    plot.setSimpleLabels(true);
    plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1}, {2})"));
    plot.setBackgroundPaint(Color.WHITE);
    plot.setInteriorGap(0.04);

    // Convert the chart to a BufferedImage
    int width = 600;
    int height = 400;
    BufferedImage chartImage = pieChart.createBufferedImage(width, height);

    return chartImage;
  }

  public static BufferedImage createCompletionBarChart(Map<String, Map<String, Integer>> fieldData, String chartTitle) {
    // Create the dataset for the bar chart
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    DefaultCategoryDataset percentageDataset = new DefaultCategoryDataset();

    for (Map.Entry<String, Map<String, Integer>> entry : fieldData.entrySet()) {
      String category = entry.getKey();
      Map<String, Integer> values = entry.getValue();
      int unfilled = values.getOrDefault("unfilled", 0);
      int filled = values.getOrDefault("filled", 0);
      int total = unfilled + filled;

      dataset.addValue(unfilled, "Unfilled", category);
      dataset.addValue(filled, "Filled", category);

      if (total > 0) {
        double percentage = (filled / (double) total);
        percentageDataset.addValue(percentage, "Filled Percentage", category);
      }
    }

    // Create the stacked bar chart
    JFreeChart stackedBarChart = ChartFactory.createStackedBarChart(
        chartTitle,          // Chart title
        "Field Category",   // Category axis label
        "Number of Fields", // Value axis label
        dataset              // Dataset
    );

    // Customize the chart
    CategoryPlot plot = (CategoryPlot) stackedBarChart.getPlot();
    plot.setBackgroundPaint(Color.WHITE); // Set background color to white

    BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
    barRenderer.setDrawBarOutline(false); // Disable bar outlines
    barRenderer.setBarPainter(new StandardBarPainter()); // Use 2D bars instead of 3D effect
    barRenderer.setShadowVisible(false); // Disable shadow to avoid 3D appearance
    barRenderer.setSeriesPaint(0, new Color(78, 121, 167, 100)); // Semi-transparent blue color for Unfilled
    barRenderer.setSeriesPaint(1, new Color(210, 49, 60));       // Solid red color for Filled
    barRenderer.setDefaultItemLabelsVisible(true);

    // Display labels only for the Filled series (index 1)
    barRenderer.setDefaultItemLabelsVisible(false); // Hide all labels initially
    barRenderer.setSeriesItemLabelsVisible(1, true); // Show labels only for Filled series
    barRenderer.setSeriesItemLabelGenerator(1, new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0")));
    barRenderer.setSeriesItemLabelPaint(1, Color.WHITE);
    barRenderer.setSeriesItemLabelFont(1, new Font("SansSerif", Font.BOLD, 10));

    // Create a new axis for percentage on the right side
    NumberAxis percentageAxis = new NumberAxis("Percentage");
    percentageAxis.setRange(0.0, 1.09);
    DecimalFormat decimalFormat = new DecimalFormat("0.0%");
    percentageAxis.setNumberFormatOverride(decimalFormat);
    plot.setRangeAxis(1, percentageAxis);

    // Add percentage dataset as a separate renderer with independent data points
    LineAndShapeRenderer percentageRenderer = new LineAndShapeRenderer();
    percentageRenderer.setSeriesPaint(0, Color.BLACK); // Use black color for the percentage points
    percentageRenderer.setDefaultShapesVisible(true); // Show data points

    Font percentageFont = new Font("SansSerif", Font.BOLD, 12);
    percentageRenderer.setDefaultItemLabelFont(percentageFont);
    percentageRenderer.setDefaultItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", decimalFormat)); // Display percentage value
    percentageRenderer.setDefaultItemLabelsVisible(true);
    percentageRenderer.setDefaultLinesVisible(false); // Do not connect data points with lines
    plot.setDataset(1, percentageDataset);
    plot.setRenderer(1, percentageRenderer);
    plot.mapDatasetToRangeAxis(1, 1);

    // Convert the chart to a BufferedImage
    int width = 600;
    int height = 400;
    BufferedImage chartImage = stackedBarChart.createBufferedImage(width, height);

    return chartImage;
  }
}
