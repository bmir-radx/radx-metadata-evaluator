package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.EvaluationResult;
import bmir.radx.metadata.evaluator.EvaluationSheetReportWriter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

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
    var reports = new ArrayList<EvaluationResult>();
    if(Files.isDirectory(filepath)){
      try(Stream<Path> paths = Files.walk(filepath)){
        paths.filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".json"))
            .forEach(file-> {
              processSingleFile(file, out);
            });
      } catch (Exception e) {
        System.err.println("Error processing files " + filepath + ": " + e.getMessage());
      }
    } else{
      processSingleFile(filepath, out);
    }
    return new EvaluationReport(reports);
  }

  private void processSingleFile(Path filepath, Path out){
    try {
      var singleReport = evaluateSingleFile(filepath, out);
      //todo write reports
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
    writer.writeSingleReport(report, sheet);
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
}
