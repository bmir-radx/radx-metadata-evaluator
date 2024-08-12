package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.variable.VariableEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.concurrent.Callable;

@Command(name="evaluateVariables")
public class VariableEvaluateCommand implements Callable<Integer> {
  private String sheetName = "Variable Evaluation Report";
  private final Workbook workbook;
  private final VariableEvaluator evaluator;

  private final EvaluationSheetReportWriter writer;

  @Option(names = "--variables", required = true, description = "Path to the variable metadata spreadsheet that you want to evaluate.")
  private Path variableMetadataFile;

  public VariableEvaluateCommand(Workbook workbook, VariableEvaluator evaluator, EvaluationSheetReportWriter writer) {
    this.workbook = workbook;
    this.evaluator = evaluator;
    this.writer = writer;
  }

  @Override
  public Integer call() throws Exception {
    if(!Files.exists(variableMetadataFile)){
      throw new FileNotFoundException("Variable Metadata File not found: " + variableMetadataFile);
    }

    var report = evaluator.evaluate(variableMetadataFile);
    var sheet = workbook.createSheet(sheetName);
    writer.writeReportHeader(sheet);
    writer.writeSingleReport(report, sheet);

    return 0;
  }
}
