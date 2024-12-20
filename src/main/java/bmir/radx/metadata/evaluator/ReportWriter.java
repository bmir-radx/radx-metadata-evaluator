package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.result.IssueDatabase;
import bmir.radx.metadata.evaluator.result.ValidationResult;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
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
    Workbook workbook = sheet.getWorkbook();
    CreationHelper createHelper = workbook.getCreationHelper();
    var linkStyle = getHyperLinkStyle(workbook);

    for (IssueDatabase.IssueDatabaseRecord record : data) {
      Row dataRow = sheet.createRow(rowIndex++);

      dataRow.createCell(0).setCellValue(record.issueId());
      dataRow.createCell(1).setCellValue(record.phs());

      // Set hyperlink for the File Location
      Cell fileLocationCell = dataRow.createCell(2);
      fileLocationCell.setCellValue(record.fileLocation());

      if (record.fileLocation() != null && !record.fileLocation().isEmpty()) {
        var hyperlink = createHelper.createHyperlink(HyperlinkType.URL);
        hyperlink.setAddress(record.fileLocation());
        fileLocationCell.setHyperlink(hyperlink);
        fileLocationCell.setCellStyle(linkStyle);
      }

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

  private CellStyle getHyperLinkStyle(Workbook workbook){
    var hyperlinkStyle = workbook.createCellStyle();
    var hyperlinkFont = workbook.createFont();
    hyperlinkFont.setUnderline(Font.U_SINGLE); // Underline
    hyperlinkFont.setColor(IndexedColors.BLUE.getIndex()); // Blue text
    hyperlinkStyle.setFont(hyperlinkFont);
    return hyperlinkStyle;
  }
}
