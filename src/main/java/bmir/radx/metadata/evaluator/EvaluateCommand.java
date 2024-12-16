package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.dataFile.DataFileEvaluator;
import bmir.radx.metadata.evaluator.result.ValidationResult;
import bmir.radx.metadata.evaluator.study.StudyEvaluator;
import bmir.radx.metadata.evaluator.variable.VariableEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import static bmir.radx.metadata.evaluator.MetadataEntity.*;

@Component
@Command(name="evaluateSingleDataFile",
    description = "Evaluate metadata for study, data file and variable entities."
)
public class EvaluateCommand implements Callable<Integer> {
  @Option(names = "--o", description = "Path to an output file where the evaluation report will be written.", required = true)
  private Path out;

  @Option(names = "--d", description = "Path to the data file metadata folder.")
  private Path datafile;

  @Option(names = "--v", description = "Path to the variable metadata spreadsheet.")
  private Path variable;

  @Option(names = "--s", description = "Path to the study metadata spreadsheet.")

  private Path study;

  private final String issueDetailsReportName = "Evaluation Report";
  private final String summaryReportName = "RADx Metadata Evaluation Report Summary";
  private final VariableEvaluator variableEvaluator;
  private final StudyEvaluator studyEvaluator;
  private final DataFileEvaluator dataFileEvaluator;
  private final EvaluationSheetReportWriter singleEntityReportWriter;
  private final ReportWriter summaryReportWriter;

  public EvaluateCommand(VariableEvaluator variableEvaluator, StudyEvaluator studyEvaluator, DataFileEvaluator dataFileEvaluator, EvaluationSheetReportWriter singleEntityReportWriter, ReportWriter summaryReportWriter) {
    this.variableEvaluator = variableEvaluator;
    this.studyEvaluator = studyEvaluator;
    this.dataFileEvaluator = dataFileEvaluator;
    this.singleEntityReportWriter = singleEntityReportWriter;
    this.summaryReportWriter = summaryReportWriter;
  }

  @Override
  public Integer call() throws Exception {
    var commandLine = new CommandLine(this);
    Map<MetadataEntity, EvaluationReport<? extends ValidationResult>> reports = new HashMap<>();

    if (variable == null && study == null && datafile == null) {
      System.err.println("Please provide at least one metadata file.");
      commandLine.usage(System.err);
      return 1;
    }

    if (variable != null) {
      if(!Files.exists(variable)){
        throw new FileNotFoundException("Variable Metadata File not found: " + variable);
      }
      var report = variableEvaluator.evaluate(variable);
      reports.put(VARIABLE_METADATA, report);
    }

    if (study != null) {
      if(!Files.exists(study)){
        throw new FileNotFoundException("Study Metadata File not found: " + study);
      }
      var report = studyEvaluator.evaluate(study);
      reports.put(STUDY_METADATA, report);
    }

    if (datafile != null){
      if(study != null){
        var report = dataFileEvaluator.evaluate(datafile, study);
        reports.put(DATA_FILE_METADATA, report);
      } else{
        var report = dataFileEvaluator.evaluate(datafile);
        reports.put(DATA_FILE_METADATA, report);
      }
    }

    // Generate single metadata entity evaluation report
    for(var entityPair: reports.entrySet()){
      generateSingleEntityReport(entityPair.getKey(), entityPair.getValue());
    }

    // Generate summary report
    generateSummaryReport(reports);
    return 0;
  }


  private void generateSingleEntityReport(MetadataEntity entity, EvaluationReport<? extends ValidationResult> report) throws IOException {
    try (var workbook = new XSSFWorkbook();
         var outputStream = getOutputStream(entity + " " + issueDetailsReportName)) {
      singleEntityReportWriter.setWorkbook(entity, workbook);
      singleEntityReportWriter.writeReports(report, out);
      workbook.write(outputStream);
    }
  }

  private void generateSummaryReport(Map<MetadataEntity, EvaluationReport<? extends ValidationResult>> reports) throws IOException {
    try (var workbook = new XSSFWorkbook();
         var outputStream = getOutputStream(summaryReportName)) {
        summaryReportWriter.writeReport(workbook, reports);

      workbook.write(outputStream);
    }
  }

  /**
   * Creates an output stream for the given file name.
   */
  private OutputStream getOutputStream(String fileName) throws IOException {
    if (out != null) {
      if (out.getParent() != null && !Files.exists(out.getParent())) {
        Files.createDirectories(out.getParent());
      }
      String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
      Path outputPath = out.resolve(fileName + " "  + timeStamp + ".xlsx");
      return Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    } else {
      throw new IllegalArgumentException("Output file must be specified.");
    }
  }
}
