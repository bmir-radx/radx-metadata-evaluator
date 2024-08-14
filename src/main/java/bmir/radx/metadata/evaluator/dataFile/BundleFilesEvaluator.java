package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static bmir.radx.metadata.evaluator.EvaluationConstant.*;

@Component
public class BundleFilesEvaluator {
  private String templateFileName = "RADxMetadataSpecification.json";
  private String sheetName = "Data File Evaluation Report";
  private final ObjectMapper mapper = new ObjectMapper();
  private final SingleFileEvaluator singleFileEvaluator;
  private final EvaluationSheetReportWriter writer;

  public BundleFilesEvaluator(SingleFileEvaluator singleFileEvaluator, EvaluationSheetReportWriter writer) {
    this.singleFileEvaluator = singleFileEvaluator;
    this.writer = writer;
  }

  public EvaluationReport evaluate(Path filepath, Path out){
    var results = new ArrayList<EvaluationResult>();
    Consumer<EvaluationResult> consumer = results::add;

    var overallCompleteness = CompletenessContainer.initiateCompletenessMap();
    var requiredCompleteness = CompletenessContainer.initiateCompletenessMap();
    var recommendedCompleteness = CompletenessContainer.initiateCompletenessMap();
    if(Files.isDirectory(filepath)){
      try(Stream<Path> paths = Files.walk(filepath)){
        paths.filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".json"))
            .forEach(file-> {
              processSingleFile(file, out, overallCompleteness, requiredCompleteness, recommendedCompleteness);
            });
      } catch (Exception e) {
        System.err.println("Error processing files " + filepath + ": " + e.getMessage());
      }
    } else{
      processSingleFile(filepath, out, overallCompleteness, requiredCompleteness, recommendedCompleteness);
    }

    consumer.accept(new EvaluationResult(EvaluationConstant.OVERALL_COMPLETENESS_DISTRIBUTION, overallCompleteness.toString()));
    consumer.accept(new EvaluationResult(EvaluationConstant.REQUIRED_COMPLETENESS_DISTRIBUTION, requiredCompleteness.toString()));
    consumer.accept(new EvaluationResult(EvaluationConstant.RECOMMENDED_COMPLETENESS_DISTRIBUTION, recommendedCompleteness.toString()));
    return new EvaluationReport(results);
  }

  private void processSingleFile(Path filepath, Path out,
                                 Map<String, Integer> overallCompleteness,
                                 Map<String, Integer> requiredCompleteness,
                                 Map<String, Integer> recommendedCompleteness){
    try {
      var singleReport = evaluateSingleFile(filepath, out);
      var overallCompletionRate = getOverallCompletionRate(singleReport, OVERALL_COMPLETION_RATE);
      CompletenessContainer.updateCompletenessDistribution(overallCompletionRate, overallCompleteness);

      var requiredCompletionRate = getOverallCompletionRate(singleReport, REQUIRED_FIELDS_COMPLETION_RATE);
      CompletenessContainer.updateCompletenessDistribution(requiredCompletionRate, requiredCompleteness);

      var recommendedCompletionRate = getOverallCompletionRate(singleReport, RECOMMENDED_FIELDS_COMPLETION_RATE);
      CompletenessContainer.updateCompletenessDistribution(recommendedCompletionRate, recommendedCompleteness);
    } catch (IOException e) {
      System.err.println("Error processing file " + filepath + ": " + e.getMessage());
    }
  }

  private Path getOutputPath(Path file, Path out){
    if(out != null){
      //Create the "single_data_file_meta_report" subfolder for single report
      Path subFolder = out.resolve("single_data_file_meta_report");
      if (!Files.exists(subFolder)) {
        try {
          Files.createDirectories(subFolder);
        } catch (IOException e) {
          throw new RuntimeException("Failed to create output directory: " + subFolder, e);
        }
      }

      String outputFileName = file.getFileName().toString().replaceAll("\\.json$", "_report.xlsx");
      return subFolder.resolve(outputFileName);
    } else{
      return null;
    }
  }

  private EvaluationReport evaluateSingleFile(Path file, Path out) throws IOException {
    var instanceNode = mapper.readTree(Files.readString(file));
    var templateNode = getTemplateNode();
    var report = singleFileEvaluator.evaluate(templateNode.toString(), instanceNode.toString());

    //write and save single data file metadata evaluation report
    var workbook = new XSSFWorkbook();
    var sheet = workbook.createSheet(sheetName);
    writer.writeReportHeader(sheet);
    writer.writeSingleReport(report, sheet, workbook, sheetName);
    var outPath = getOutputPath(file, out);
    var outputStream = Files.newOutputStream(outPath);
    workbook.write(outputStream);

    return singleFileEvaluator.evaluate(templateNode.toString(), instanceNode.toString());
  }

  private JsonNode getTemplateNode(){
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream inputStream = classLoader.getResourceAsStream(templateFileName)) {
      if (inputStream == null) {
        throw new IllegalArgumentException("File not found! " + "RADxMetadataSpecification.json");
      } else {
        return mapper.readTree(inputStream);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private double getOverallCompletionRate(EvaluationReport report, EvaluationConstant evaluationConstant) {
    for (var result : report.results()) {
      if (evaluationConstant.equals(result.getEvaluationConstant())) {
        return Double.parseDouble(result.getContent());
      }
    }
    throw new IllegalArgumentException("OVERALL_COMPLETION_RATE not found in the report.");
  }
}
