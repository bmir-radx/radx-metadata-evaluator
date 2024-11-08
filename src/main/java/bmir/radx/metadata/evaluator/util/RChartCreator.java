package bmir.radx.metadata.evaluator.util;

import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static bmir.radx.metadata.evaluator.util.FieldCategory.getCategoryNames;

public class RChartCreator {
  public static void main(String[] args) {
    try {
      // Connect to Rserve
      RConnection connection = new RConnection();

//      // Load necessary libraries, installing them if needed
//      connection.eval("if (!require('ggplot2')) install.packages('ggplot2', repos='http://cran.rstudio.com/')");
//      connection.eval("library(ggplot2)");
//      connection.eval("if (!require('webr')) install.packages('webr', repos='http://cran.rstudio.com/')");
//      connection.eval("library(webr)");

      // Ring chart
      String outputPath = Paths.get(System.getProperty("user.dir"), "ring_chart.png").toString();
//      generateRingChart(connection, Map.of("Invalid URL", 170, "Inconsistent", 50, "Unknown", 10), outputPath, "Issues");

      // Stacked bar chart
//      int[][] data = {
//          {234, 43, 1520, 234}, // filled counts for each category
//          {123, 23, 4565, 2312}   // unfilled counts for each category
//      };
//      outputPath = Paths.get(System.getProperty("user.dir"), "stacked_chart.png").toString();
//      generateStackedBarScatter(connection, outputPath, data);

      // Bar chart
//      outputPath = Paths.get(System.getProperty("user.dir"), "bar_charts.png").toString();
//      Map<String, Integer> barData = new HashMap<>();
//      barData.put("COVID", 272);
//      barData.put("ROR", 272);
//      barData.put("ORCID", 544);
//      generateCTDistributionChart(connection, barData, outputPath);

      outputPath = Paths.get(System.getProperty("user.dir"), "histogram.png").toString();
      Map<Integer, Integer> histogramData = new HashMap<>();
      histogramData.put(3, 12);
      histogramData.put(8, 2);
      generateHistogramChart(connection, histogramData, outputPath, "Required");

      // Close the connection to Rserve
      connection.close();

      System.out.println("Ring chart generated successfully!");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static BufferedImage generateRingChart(RConnection connection, Map<String, Integer> data, String outputPath, String centerLabel) throws Exception {
    // Check and install dplyr and ggplot2 if they are not installed
    connection.eval("if (!requireNamespace('dplyr', quietly = TRUE)) install.packages('dplyr', repos='http://cran.us.r-project.org')");
    connection.eval("if (!requireNamespace('ggplot2', quietly = TRUE)) install.packages('ggplot2', repos='http://cran.us.r-project.org')");

    // Load required libraries
    connection.eval("library(ggplot2)");
    connection.eval("library(dplyr)");

    // Prepare the data for R
    StringBuilder rData = new StringBuilder("data <- data.frame(category = c(");
    data.keySet().forEach(key -> rData.append("'" + key + "', "));
    rData.delete(rData.length() - 2, rData.length()).append("), values = c(");
    data.values().forEach(value -> rData.append(value + ", "));
    rData.delete(rData.length() - 2, rData.length()).append("))");
    connection.eval(rData.toString());

    // Calculate total records
    connection.eval("total_records <- sum(data$values)");

    // Add percentage calculation without using the pipe operator
    connection.eval("data$percentage <- data$values / total_records * 100");

    // Generate the ring chart with automatic colors and center annotation
    String ringChartCommand =
        "ring_chart <- ggplot(data, aes(x = 2, y = values, fill = category)) + " +
            "geom_col(width = 0.5, color = 'white') + " +
            "coord_polar(theta = 'y') + " +
            "xlim(0.5, 2.5) + " +
            "theme_void() + " +
            "theme(legend.position = 'none') + " +  // Remove legend
            "geom_text(aes(label = paste(category, '\\n', values, ' (', round(percentage), '%)', sep = '')), " +
            "position = position_stack(vjust = 0.5), color = 'black', size = 4, fontface = 'bold') + " +
            // Separate annotations for total and label with different sizes and adjusted vjust
            "annotate('text', x = 0.5, y = 0, label = total_records, size = 8, fontface = 'bold', vjust = -0.5) + " +
            "annotate('text', x = 0.5, y = 0, label = '" + centerLabel + "', size = 4, color = 'black', vjust = 1.5)";
    connection.eval(ringChartCommand);

    String saveChartCommand = String.format("ggsave('%s', plot = ring_chart, width = 6, height = 4)", outputPath);
    connection.eval(saveChartCommand);
    return getImage(outputPath);
  }

  public static BufferedImage generateStackedBarScatter(RConnection connection, int[][] data, String outputPath) throws REngineException {
    int[] filled = data[0];
    int[] unfilled = data[1];

    String[] categories = getCategoryNames();

    // Send data to R
    connection.assign("categories", categories);
    connection.assign("filled", filled);
    connection.assign("unfilled", unfilled);

    // Calculate total for each category in R (filled + unfilled)
    connection.eval("total <- filled + unfilled");

    // Calculate percentages for scatter plot
    connection.eval("percentages <- (filled / total) * 100");

    // R script to check and install required packages if not present
    String checkAndInstallPackages =
        "required_packages <- c('ggplot2', 'scales');" +
            "for (pkg in required_packages) {" +
            "  if (!requireNamespace(pkg, quietly = TRUE)) {" +
            "    install.packages(pkg, repos = 'http://cran.us.r-project.org');" +
            "  }" +
            "};" +
            "library(ggplot2);" +
            "library(scales);";
    connection.eval(checkAndInstallPackages);

    // R script for creating the plot
    String rScript =
        "data <- data.frame(categories = factor(categories, levels = categories), filled = filled, unfilled = unfilled + filled, percentages = percentages);" +
            "p <- ggplot(data) + " +

            // Draw unfilled bars as the base layer
            "  geom_bar(aes(x = categories, y = unfilled, fill = 'Unfilled'), stat = 'identity') + " +

            // Draw filled bars on top, starting from the height of unfilled
            "  geom_bar(aes(x = categories, y = filled, fill = 'Filled'), stat = 'identity', position = position_stack(reverse = TRUE)) + " +
            "  scale_fill_manual(values = c('Filled' = 'forestgreen', 'Unfilled' = 'lightgrey')) + " +

            // Display the value of 'filled' in the middle of the filled section
            "  geom_text(aes(x = categories, y = filled / 2 , label = filled), color = 'black', vjust = 0.5, fontface = 'bold') + " +

            // Scatter plot for percentage values (scale percentages to match Count scale for positioning)
            "  geom_point(aes(x = categories, y = percentages * max(total) / 100, color = 'Filled Fields Percentage'), size = 3) + " +

            // Display the percentage value above each scatter point
            "  geom_text(aes(x = categories, y = percentages * max(total) / 100, label = paste0(round(percentages, 1), '%')), vjust = -0.5, color = 'blue') + " +

            // Set up labels and themes
            "  labs(y = 'Count', x = 'Category') + " +
            "  theme_minimal() + " +

            // Set the background to solid white
            "  theme(panel.background = element_rect(fill = 'white', color = 'white'), " +
            "        plot.background = element_rect(fill = 'white', color = 'white'), " +
            "        legend.position = 'bottom', " +
            "        legend.direction = 'horizontal', " +
            "        legend.title = element_blank()) + " +

            // Adjust y-axis to allow better visibility of stacked bars
            "  scale_y_continuous(name = 'Count', limits = c(0, max(total) * 1.5), " +
            "                     sec.axis = sec_axis(~ . * (100 / max(total)), name = 'Percentage', labels = function(x) paste0(x, '%'))) + " +
            "  scale_color_manual(values = c('Filled Fields Percentage' = 'blue'));";
    connection.eval(rScript);

    // Increase plot size to ensure the legend is visible
    String saveChartCommand = String.format("ggsave('%s', plot = p, width = 8, height = 6)", outputPath);
    connection.eval(saveChartCommand);

    return getImage(outputPath);
  }


  public static BufferedImage generateCTDistributionChart(RConnection connection, Map<String, Integer> data, String outputPath) throws Exception {
    connection.eval("if (!requireNamespace('ggplot2', quietly = TRUE)) install.packages('ggplot2', repos='http://cran.us.r-project.org')");
    connection.eval("library(ggplot2)");

    // Prepare data for R
    StringBuilder keys = new StringBuilder("c(");
    StringBuilder values = new StringBuilder("c(");

    for (Map.Entry<String, Integer> entry : data.entrySet()) {
      keys.append("\"").append(entry.getKey()).append("\", ");
      values.append(entry.getValue()).append(", ");
    }

    // Remove trailing commas and close vectors
    if (keys.length() > 2) {
      keys.delete(keys.length() - 2, keys.length()).append(")");
    } else {
      keys.append(")");
    }
    if (values.length() > 2) {
      values.delete(values.length() - 2, values.length()).append(")");
    } else {
      values.append(")");
    }

    // Assign data in R
    connection.eval("keys <- " + keys.toString());
    connection.eval("values <- " + values.toString());

    // Use ggplot2 to create and save the chart as an image
    String rScript =
        "data <- data.frame(keys=keys, values=values); " +
            "p <- ggplot(data, aes(x=keys, y=values)) + " +
            "geom_bar(stat='identity', fill='blue') + " +
            "geom_text(aes(label=values), vjust=-0.3, size=4) + " +
            "theme_minimal() + " +
            "theme(axis.text.x = element_text(angle = 60, hjust = 1, color='black')) + " +
            "xlab('Controlled Terms') + ylab('Count');";
    connection.eval(rScript);

    String saveChartCommand = String.format("ggsave('%s', plot = p, width = 6, height = 4)", outputPath);
    connection.eval(saveChartCommand);

    // Save the generated image
    return getImage(outputPath);
  }

  public static BufferedImage generateHistogramChart(RConnection connection, Map<Integer, Integer> data, String outputPath, String fieldCategory) throws Exception {
    connection.eval("if (!requireNamespace('ggplot2', quietly = TRUE)) install.packages('ggplot2', repos='http://cran.us.r-project.org')");
    connection.eval("library(ggplot2)");

    // Prepare data for R
    int maxKey = data.keySet().stream().max(Integer::compare).orElse(0);
    Map<Integer, Integer> completeDistribution = new HashMap<>();
    IntStream.rangeClosed(0, maxKey).forEach(i -> completeDistribution.put(i, data.getOrDefault(i, 0)));

    StringBuilder filledFields = new StringBuilder();
    StringBuilder frequencies = new StringBuilder();

    for (Map.Entry<Integer, Integer> entry : completeDistribution.entrySet()) {
      filledFields.append(entry.getKey()).append(",");
      frequencies.append(entry.getValue()).append(",");
    }

    // Remove trailing commas
    if (filledFields.length() > 0) {
      filledFields.setLength(filledFields.length() - 1);
      frequencies.setLength(frequencies.length() - 1);
    }

    // Assign data to R variables
    connection.eval("filledFields <- c(" + filledFields.toString() + ")");
    connection.eval("frequencies <- c(" + frequencies.toString() + ")");

    // Use ggplot2 to create and save the chart as an image
    String rScript = String.format(
        "data <- data.frame(FilledFields = filledFields, Frequency = frequencies);" +
            "p <- ggplot(data, aes(x=factor(FilledFields), y=Frequency)) + " +
                "geom_bar(stat='identity', fill='blue', color='black') + " +
                "xlab('Filled Fields Number - %s') + ylab('Metadata Record') + " +
                "geom_text(aes(label=Frequency), vjust=-0.5) + " +
                "theme_minimal();", fieldCategory);

    connection.eval(rScript);

    String saveChartCommand = String.format("ggsave('%s', plot = p, width = 6, height = 4)", outputPath);
    connection.eval(saveChartCommand);

    // Save the generated image
    return getImage(outputPath);
  }



  private static BufferedImage getImage(String outputPath){
    BufferedImage bufferedImage = null;
    try {
      File file = new File(outputPath);
      bufferedImage = ImageIO.read(file);
      int targetWidth = 600;
      int targetHeight = 400;
      return resizeImage(bufferedImage, targetWidth, targetHeight);
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Error reading the saved image file: " + outputPath, e);
    }
  }
  private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
    Image scaledImage = originalImage.getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
    BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);

    Graphics2D g2d = resizedImage.createGraphics();
    g2d.drawImage(scaledImage, 0, 0, null);
    g2d.dispose();

    return resizedImage;
  }
}
