package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.result.IssueDatabase;
import bmir.radx.metadata.evaluator.result.ValidationResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static bmir.radx.metadata.evaluator.statistics.SummaryReportCalculator.groupByStudyPhs;

@Component
public class ReportWriter {
  private final String SHEET_NAME = "Issues Database";

  public void writeReport(Workbook workbook, Map<MetadataEntity, EvaluationReport<? extends ValidationResult>> reports){
    Sheet sheet = workbook.createSheet(SHEET_NAME);
    var data = groupByStudyPhs(reports);
    writeHeaders(sheet);
    writeData(sheet, data);
  }

  private void writeHeaders(Sheet sheet) {
    Row headerRow = sheet.createRow(0);
    String[] headers = {
        "Issue ID", "PHS", "File location", "File location comments", "Entity Type",
        "Metadata Field", "Value", "Start Position", "End Position", "Issue Type",
        "Issue Description", "Issue Level", "Repair Suggestion", "Evaluation Date",
        "Issue Creator", "Comments"
    };

    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
    }
  }

  private void writeData(Sheet sheet, List<IssueDatabase.IssueDatabaseRecord> data) {
    int rowIndex = 1;

    for (IssueDatabase.IssueDatabaseRecord record : data) {
      Row dataRow = sheet.createRow(rowIndex++);

      dataRow.createCell(0).setCellValue(record.issueId());
      dataRow.createCell(1).setCellValue(record.phs());
      dataRow.createCell(2).setCellValue(record.fileLocation());
      dataRow.createCell(3).setCellValue(record.fileLocationComments());
      dataRow.createCell(4).setCellValue(record.entityType());
      dataRow.createCell(5).setCellValue(record.metadataField());
      dataRow.createCell(6).setCellValue(record.originValue());
      dataRow.createCell(7).setCellValue(record.startPosition());
      dataRow.createCell(8).setCellValue(record.endPosition());
      dataRow.createCell(9).setCellValue(record.issueType());
      dataRow.createCell(10).setCellValue(record.issueDescription());
      dataRow.createCell(11).setCellValue(record.issueLevel());
      dataRow.createCell(12).setCellValue(record.repairSuggestion());
      dataRow.createCell(13).setCellValue(record.evaluationDate().toString());
      dataRow.createCell(14).setCellValue(record.issueCreator());
      dataRow.createCell(15).setCellValue(record.comments());
    }
  }
}
