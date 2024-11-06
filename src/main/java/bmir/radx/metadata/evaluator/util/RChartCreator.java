package bmir.radx.metadata.evaluator.util;

import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RConnection;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;

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

      // Example usage
      String outputPath = Paths.get(System.getProperty("user.dir"), "ring_chart.png").toString();
//      generateRingChart(connection, Map.of("Invalid URL", 170, "Inconsistent", 50, "Unknown", 10), outputPath, "Issues");

      int[][] data = {
          {234, 43, 1520, 234}, // filled counts for each category
          {123, 23, 4565, 2312}   // unfilled counts for each category
      };
      outputPath = Paths.get(System.getProperty("user.dir"), "stacked_chart.png").toString();
      generateStackedBarScatter(connection, outputPath, data);

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

    String saveChartCommand = "ggsave('" + outputPath + "', plot = ring_chart, width = 6, height = 4)";
    connection.eval(saveChartCommand);
    return getImage(outputPath);
  }

  public static BufferedImage generateStackedBarScatter(RConnection connection, String outputPath, int[][] data) throws REngineException {
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
            "  geom_point(aes(x = categories, y = percentages * max(total) / 100, color = 'Percentage'), size = 3) + " +

            // Display the percentage value above each scatter point
            "  geom_text(aes(x = categories, y = percentages * max(total) / 100, label = paste0(round(percentages, 1), '%')), vjust = -0.5, color = 'blue') + " +

            // Set up labels and themes
            "  labs(y = 'Count', x = 'Category') + " +
            "  theme_minimal() + " +

            // Adjust y-axis to allow better visibility of stacked bars
            "  scale_y_continuous(name = 'Count', limits = c(0, max(total) * 1.5), " +
            "                     sec.axis = sec_axis(~ . * (100 / max(total)), name = 'Percentage', labels = function(x) paste0(x, '%'))) + " +
            "  scale_color_manual(values = c('Percentage' = 'blue')) + " +
            "  theme(legend.position = 'bottom', legend.title = element_blank());";
    connection.eval(rScript);


    System.out.println("Image generated at: " + outputPath);

    String saveChartCommand = "ggsave('" + outputPath + "', plot = p, width = 6, height = 4)";
    connection.eval(saveChartCommand);

    return getImage(outputPath);
  }

  public static void startRServe() {
    try {
      // Check if RServe is already running by trying to connect
      var rConnection = new org.rosuda.REngine.Rserve.RConnection();
      System.out.println("RServe is already running.");
      rConnection.close();
    } catch (Exception e) {
      System.out.println("RServe is not running. Attempting to start RServe...");

      try {
        // Determine command based on OS type
        String command;
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
          command = "R CMD Rscript -e \"if (!requireNamespace('Rserve', quietly = TRUE)) install.packages('Rserve'); library(Rserve); Rserve(args = '--no-save')\"";
          new ProcessBuilder("cmd.exe", "/c", command).start();
        } else {
          command = "R CMD Rscript -e \"if (!requireNamespace('Rserve', quietly = TRUE)) install.packages('Rserve'); library(Rserve); Rserve(args = '--no-save')\"";
          ProcessBuilder processBuilder = new ProcessBuilder("sh", "-c", command);
          processBuilder.redirectErrorStream(true);
          Process process = processBuilder.start();

          // Capture output to check for errors
          BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
          String line;
          while ((line = reader.readLine()) != null) {
            System.out.println(line); // Print command output for debugging
          }
        }

        // Retry loop: Wait for RServe to start, retrying up to 5 times
        int maxRetries = 5;
        int retryCount = 0;
        boolean isConnected = false;
        while (retryCount < maxRetries && !isConnected) {
          try {
            Thread.sleep(2000); // Wait 2 seconds before each retry
            var rConnection = new org.rosuda.REngine.Rserve.RConnection();
            System.out.println("RServe started successfully.");
            rConnection.close();
            isConnected = true; // Connection successful
          } catch (Exception retryException) {
            retryCount++;
            System.out.println("Waiting for RServe to start... (Attempt " + retryCount + ")");
          }
        }

        if (!isConnected) {
          System.err.println("Failed to start RServe after multiple attempts.");
        }

      } catch (Exception ex) {
        ex.printStackTrace();
      }
    }
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
