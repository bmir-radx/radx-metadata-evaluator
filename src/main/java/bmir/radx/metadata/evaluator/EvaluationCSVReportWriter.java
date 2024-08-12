package bmir.radx.metadata.evaluator;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class EvaluationCSVReportWriter {
  public void writeReportHeader(OutputStream outputStream) throws IOException {
    var resultWriter = new CSVPrinter(new PrintStream(outputStream, true, StandardCharsets.UTF_8), CSVFormat.DEFAULT);
    resultWriter.printRecord("EVALUATION TYPE", "Content");
  }

  public void writeReport(EvaluationReport report, OutputStream outputStream) throws IOException {
    var resultWriter = new CSVPrinter(new PrintStream(outputStream, true, StandardCharsets.UTF_8), CSVFormat.DEFAULT);

    report.results()
        .stream()
        .map(EvaluationCSVReportWriter::toCsvRecord)
        .forEach(r -> {
          try {
            resultWriter.printRecord(r);
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  private static List<? extends Serializable> toCsvRecord(EvaluationResult r) {
    var type = r.getEvaluationConstant();
    var content = r.getContent();

    return List.of(type, content);
  }
}
