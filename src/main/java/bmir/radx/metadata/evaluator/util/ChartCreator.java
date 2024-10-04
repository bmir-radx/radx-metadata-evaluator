package bmir.radx.metadata.evaluator.util;

import org.apache.poi.ss.usermodel.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Map;

public class ChartCreator {
  public static <K> BufferedImage createChartImage(Map<K, Integer> distributionMap, String title, String sheetName) {
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

    return createChartFromDataset(dataset, title, sheetName);
  }

  // Common method to generate chart from dataset
  private static BufferedImage createChartFromDataset(DefaultCategoryDataset dataset, String title, String sheetName) {
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

    // Set the bar color (apply to series 0)
    renderer.setSeriesPaint(0, Color.BLUE); // Change Color.BLUE to any color you prefer

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
}
