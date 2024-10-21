package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.*;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.util.ReportAggregator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
public class BundleFilesEvaluator {
  private String templateFileName = "RADxMetadataSpecification.json";
  private final ObjectMapper mapper = new ObjectMapper();
  private final SingleFileEvaluator singleFileEvaluator;
  private final ReportAggregator reportAggregator;

  public BundleFilesEvaluator(SingleFileEvaluator singleFileEvaluator, ReportAggregator reportAggregator) {
    this.singleFileEvaluator = singleFileEvaluator;
    this.reportAggregator = reportAggregator;
  }

  public EvaluationReport<JsonValidationResult> evaluate(Path filepath, Path out){
    var results = new ArrayList<EvaluationResult>();
    Consumer<EvaluationResult> consumer = results::add;

    if(Files.isDirectory(filepath)){
      try(Stream<Path> paths = Files.walk(filepath)){
        paths.filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".json"))
            .forEach(file-> {
              processSingleFile(file, out, reportAggregator);
            });
      } catch (Exception e) {
        System.err.println("Error processing files " + filepath + ": " + e.getMessage());
      }
    } else{
      processSingleFile(filepath, out, reportAggregator);
    }

    reportAggregator.addSummaryResults(consumer);
    return new EvaluationReport<>(results, reportAggregator.getValidationErrors());
  }

  private void processSingleFile(Path filepath, Path out, ReportAggregator reportAggregator){
    try {
      var singleReport = evaluateSingleFile(filepath, out);
      reportAggregator.aggregate(singleReport);
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

  private EvaluationReport<JsonValidationResult> evaluateSingleFile(Path file, Path out) throws IOException {
    var instanceNode = mapper.readTree(Files.readString(file));
    var templateNode = getTemplateNode();
    var report = singleFileEvaluator.evaluate(templateNode.toString(), instanceNode.toString());

//    //write and save single data file metadata evaluation report
//    var workbook = new XSSFWorkbook();
//    var sheet = workbook.createSheet(sheetName);
//    writer.writeEvaluationReportHeader(sheet);
//    writer.writeEvaluationReport(report, sheet, sheetName);
//    var outPath = getOutputPath(file, out);
//    var outputStream = Files.newOutputStream(outPath);
//    workbook.write(outputStream);

    return report;
  }

  private JsonNode getTemplateNode(){
    ClassLoader classLoader = getClass().getClassLoader();
    try (InputStream inputStream = classLoader.getResourceAsStream(templateFileName)) {
      if (inputStream == null) {
        throw new IllegalArgumentException("File not found: " + "RADxMetadataSpecification.json");
      } else {
        return mapper.readTree(inputStream);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
