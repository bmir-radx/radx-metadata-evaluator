package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.dataFile.BundleFilesEvaluator;
import bmir.radx.metadata.evaluator.result.Result;
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
import java.util.List;
import java.util.concurrent.Callable;

@Component
@Command(name="evaluate",
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

  private final String variableSheetName = "Variable Evaluation Report";
  private final String dataFileSheetName = "Data File Evaluation Report";
  private final String studySheetName = "Study Evaluation Report";
  private final String studyValidationSheetName = "Study Validation Report";
  private final VariableEvaluator variableEvaluator;
  private final StudyEvaluator studyEvaluator;
  private final BundleFilesEvaluator dataFileEvaluator;
  private final EvaluationSheetReportWriter writer;

  public EvaluateCommand(VariableEvaluator variableEvaluator, StudyEvaluator studyEvaluator, BundleFilesEvaluator dataFileEvaluator, EvaluationSheetReportWriter writer) {
    this.variableEvaluator = variableEvaluator;
    this.studyEvaluator = studyEvaluator;
    this.dataFileEvaluator = dataFileEvaluator;
    this.writer = writer;
  }

  private OutputStream getOutputStream() throws IOException {
    if (out != null) {
      if (out.getParent() != null && !Files.exists(out.getParent())) {
        Files.createDirectories(out.getParent());
      }
      String timeStamp = new SimpleDateFormat("yyyyMMdd").format(new Date());
      Path outputPath = out.resolve("Evaluation_Report_" + timeStamp + ".xlsx");
      return Files.newOutputStream(outputPath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    } else {
      throw new IllegalArgumentException("Output file must be specified.");
    }
  }

  @Override
  public Integer call() throws Exception {
    var commandLine = new CommandLine(this);
    var reports = new HashMap<String, List<? extends Result>>();

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
      reports.put(variableSheetName, report.evaluationResults());
    }

    if (study != null) {
      if(!Files.exists(study)){
        throw new FileNotFoundException("Study Metadata File not found: " + study);
      }
      var report = studyEvaluator.evaluate(study);
//      reports.put(studySheetName, report.evaluationResults());
//      reports.put(studyValidationSheetName, report.validationResults());
    }

    if (datafile != null){
      var report = dataFileEvaluator.evaluate(datafile, out);
      reports.put(dataFileSheetName, report.evaluationResults());
    }

    var workbook = new XSSFWorkbook();
    writer.writeReports(workbook, reports);

    var outputStream = getOutputStream();
    workbook.write(outputStream);

    if(outputStream != System.out) {
      outputStream.close();
    }

    return 0;
  }
}
