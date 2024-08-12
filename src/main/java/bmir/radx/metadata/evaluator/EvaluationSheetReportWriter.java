package bmir.radx.metadata.evaluator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class EvaluationSheetReportWriter {
  public void writeReportHeader(Sheet sheet) {
    Row headerRow = sheet.createRow(0);
    Cell headerCell0 = headerRow.createCell(0);
    headerCell0.setCellValue("EVALUATION TYPE");
    Cell headerCell1 = headerRow.createCell(1);
    headerCell1.setCellValue("Content");
  }

  public void writeSingleReport(EvaluationReport report, Sheet sheet) {
    int rowIndex = 1; // Starting row index for data
    for (EvaluationResult r : report.results()) {
      Row row = sheet.createRow(rowIndex++);
      row.createCell(0).setCellValue(r.getEvaluationConstant().name());
      row.createCell(1).setCellValue(r.getContent());
    }
  }

  public void writeReports(Workbook workbook, Map<String, EvaluationReport> reports){
    for(var entrySet: reports.entrySet()){
      var sheetName = entrySet.getKey();
      var report = entrySet.getValue();
      var sheet = workbook.createSheet(sheetName);
      writeReportHeader(sheet);
      writeSingleReport(report, sheet);
    }
  }
}
