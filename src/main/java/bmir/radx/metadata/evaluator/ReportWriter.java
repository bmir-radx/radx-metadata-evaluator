package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.result.IssueDatabase;
import bmir.radx.metadata.evaluator.result.ValidationResult;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bmir.radx.metadata.evaluator.statistics.SummaryReportCalculator.convert2IssueDatabaseFormat;
import static bmir.radx.metadata.evaluator.statistics.SummaryReportCalculator.convert2SummaryReportFormat;

@Component
public class ReportWriter {
  private final String ISSUE_DATABSE_SHEET_NAME = "Issues Database";
  private final String METADATA_INSTANCES_SUMMARY = "Metadata Instances Summary";
  private final String[] HEADERS = new String[] {
      "Overall Completeness",
      "Required Fields Completeness",
      "Recommended Fields Completeness",
      "Optional Fields Completeness",
      "Consistency",
      "#Consistency Issues",
      "Accuracy",
      "#Accuracy Issues",
      "Validity",
      "#Validity Issues",
      "Accessibility",
      "#Accessibility Issues",
      "Controlled Vocabulary Consistency",
      "#Controlled Vocabulary Consistency Issues",
      "Uniqueness",
      "#Uniqueness Issues",
      "Linguistic Quality",
      "#Linguistic Quality Issues",
      "Structural Quality",
      "#Structural Quality Issues"
  };
  private final int MAX_CELL_LENGTH = 32767;

  public void writeIssuesDatabase(Workbook workbook, Map<MetadataEntity, EvaluationReport<? extends ValidationResult>> reports){
    Sheet sheet = workbook.createSheet(ISSUE_DATABSE_SHEET_NAME);
    var data = convert2IssueDatabaseFormat(reports);
    writeIssueDatabaseHeaders(sheet);
    writeIssueDatabaseData(sheet, data);
  }

  public void writeMetadataInstanceSummary(Workbook workbook, Map<MetadataEntity, EvaluationReport<? extends ValidationResult>> reports){
    Sheet sheet = workbook.createSheet(METADATA_INSTANCES_SUMMARY);
    var data = convert2SummaryReportFormat(reports);
    writeMetadataSummaryHeaders(sheet);
    writeMetadataSummaryData(workbook, sheet, data);
  }

  private void writeMetadataSummaryHeaders(Sheet sheet){

    // 1. Write row 0: "Study Metadata", "Data File Metadata", "Totals"
    Row row0 = sheet.createRow(0);
    // Study Metadata spans columns [1...20]
    row0.createCell(1).setCellValue("Study Metadata");
    for (int i = 2; i < 21; i++) {
      row0.createCell(i).setCellValue(""); // Merge visually if desired
    }
    // Merge columns 0..19 into a single cell for "Study Metadata"
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 1, 20));

    // Data File Metadata spans columns [21..40]
    row0.createCell(21).setCellValue("Data File Metadata");
    for (int i = 22; i < 41; i++) {
      row0.createCell(i).setCellValue("");
    }
    // Merge columns 21..40
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 21, 40));

    // Totals go in columns 41..43
    row0.createCell(41).setCellValue("Totals");
    row0.createCell(42).setCellValue("");
    row0.createCell(43).setCellValue("");
    sheet.addMergedRegion(new CellRangeAddress(0, 0, 41, 43));

    // 2. Write row 1: column headers
    Row row1 = sheet.createRow(1);

    // Fill columns 0..19 for Study, 20..39 for Data File
    for (int i = 0; i < HEADERS.length; i++) {
      row1.createCell(i + 1).setCellValue(HEADERS[i]);
      row1.createCell(i + 21).setCellValue(HEADERS[i]);
    }

    // Last 3 columns: Totals
    row1.createCell(41).setCellValue("Total Study Metadata Issues");
    row1.createCell(42).setCellValue("Total Data File Metadata Issues");
    row1.createCell(43).setCellValue("Total Issues");
  }

  private void writeMetadataSummaryData(Workbook workbook, Sheet sheet, Map<String, Map<String, Map<String, Object>>> groupedData){
    int rowIndex = 2;
    // For each PHS
    for (var phs : groupedData.keySet()) {
      // Create a new row
      Row dataRow = sheet.createRow(rowIndex++);
      // Column 0: PHS
      dataRow.createCell(0).setCellValue(phs);

      // Get the maps for each MetadataEntity if present
      // (Study Metadata, Data File Metadata). If missing, use an empty map.
      Map<String, Object> studyData = groupedData
          .get(phs)
          .getOrDefault(MetadataEntity.STUDY_METADATA.getEntityName(), Map.of());
      Map<String, Object> fileData = groupedData
          .get(phs)
          .getOrDefault(MetadataEntity.DATA_FILE_METADATA.getEntityName(), Map.of());

      // Fill columns [1..20] for Study Metadata (we already used col 0 for PHS)
      fillEntityData(workbook, dataRow, 1, studyData, HEADERS);

      // Fill columns [21..40] for Data File Metadata
      fillEntityData(workbook, dataRow, 21, fileData, HEADERS);

      // 1) Compute total study issues
      int totalStudyIssues = computeTotalIssues(studyData);

      // 2) Compute total data file issues
      int totalFileIssues = computeTotalIssues(fileData);

      // 3) Compute total issues overall
      int totalAllIssues = totalStudyIssues + totalFileIssues;

      // 4) Write them into columns [41..43]
      dataRow.createCell(41).setCellValue(totalStudyIssues);
      dataRow.createCell(42).setCellValue(totalFileIssues);
      dataRow.createCell(43).setCellValue(totalAllIssues);
    }
  }

  /**
   * Fills a single row of data for one entity (Study or Data File),
   * starting at startCol. HEADERS contains the column order:
   * - Some are completeness columns (store as a double or percentage).
   * - Others are issue “Yes/No” columns, with a corresponding “#xxx Issues” column.
   */
  private void fillEntityData(
      Workbook workbook,
      Row dataRow,
      int startCol,
      Map<String, Object> entityData,
      String[] HEADERS
  ) {
    int colOffset = 0;

    // Create a percentage CellStyle
    CellStyle percentStyle = workbook.createCellStyle();
    DataFormat format = workbook.createDataFormat();
    percentStyle.setDataFormat(format.getFormat("0.00%"));

    for (int i = 0; i < HEADERS.length; i++) {
      String header = HEADERS[i];
      int currentCol = startCol + i;
      Cell cell = dataRow.createCell(currentCol);

      // If this header is one of the completeness columns, we expect a Double.
      if (header.contains("Completeness")) {
        // Default to 0.0 if missing
        double completeness = 0.0;
        if (entityData.containsKey(header)) {
          Object val = entityData.get(header);
          if (val instanceof Number num) {
            completeness = num.doubleValue();
          }
        }
        if (completeness == 0.0) {
          // Set cell value to "NA" as a string
          cell.setCellValue("NA");
          CellStyle naStyle = workbook.createCellStyle();
          Font naFont = workbook.createFont();
          naFont.setItalic(true);
          naFont.setColor(IndexedColors.GREY_25_PERCENT.getIndex());
          naStyle.setFont(naFont);
          cell.setCellStyle(naStyle);
        } else {
          // Set cell value as a percentage
          cell.setCellValue(completeness);
          cell.setCellStyle(percentStyle);
        }
      }
      else if (header.startsWith("#")) {
        // This is the issue-count column (e.g., "#Consistency Issues").
        // The corresponding criterion name is the header substring after ‘#’ and before " Issues".
        // For example, "#Consistency Issues" -> "Consistency"
        String criterion = header.replace("#", "").replace(" Issues", "").trim();

        // The entityData might store the “issue count” as the exact key ("Consistency"),
        // so let’s see if we can find it. If not found, treat as zero.
        int count = 0;
        if (entityData.containsKey(criterion)) {
          Object val = entityData.get(criterion);
          if (val instanceof Number num) {
            count = num.intValue();
          }
        }
        cell.setCellValue(count);
      }
      else {
        // This is the “criterion name” column (e.g., "Consistency", "Accuracy", etc.),
        // which we display as “Yes” if count == 0, or “No” if count > 0.
        // Let’s see if we can get the integer from entityData with the same key.
        int count = 0;
        if (entityData.containsKey(header)) {
          Object val = entityData.get(header);
          if (val instanceof Number num) {
            count = num.intValue();
          }
        }
        // If count == 0 => “Yes” (meaning no issues found),
        // else => “No”
        String yesNo = (count == 0) ? "Yes" : "No";
        cell.setCellValue(yesNo);
      }
    }
  }

  /**
   * Sums the issue counts for all non-completeness fields
   * in entityData that have a numeric value > 0.
   */
  private int computeTotalIssues(Map<String, Object> entityData) {
    int total = 0;
    for (var entry : entityData.entrySet()) {
      String key = entry.getKey();
      // Skip any completeness fields
      if (key.contains("Completeness")) {
        continue;
      }
      // This is presumably an issue if it's a number
      Object val = entry.getValue();
      if (val instanceof Number num) {
        int issueCount = num.intValue();
        if (issueCount > 0) {
          total += issueCount;
        }
      }
    }
    return total;
  }

  private void writeIssueDatabaseHeaders(Sheet sheet) {
    Row headerRow = sheet.createRow(0);
    String[] headers = {
        "Result ID", "PHS", "File Location", "File Location Comments", "Entity Type",
        "Metadata Field", "Value", "Start Position", "End Position", "Criterion",
        "Description", "Result Level", "Repair Suggestion", "Evaluation Date",
        "Creator", "Fixed Locally", "Applied to Hub", "Comments"
    };

    for (int i = 0; i < headers.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(headers[i]);
    }
  }

  private void writeIssueDatabaseData(Sheet sheet, List<IssueDatabase.IssueDatabaseRecord> data) {
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
      dataRow.createCell(5).setCellValue(truncateValue(record.metadataField()));
      dataRow.createCell(6).setCellValue(truncateValue(record.originValue()));

      // Handle 'startPosition' with null check
      Cell startPosCell = dataRow.createCell(7);
      if (record.startPosition() != null) {
        startPosCell.setCellValue(record.startPosition());
      } else {
        startPosCell.setCellValue("");
      }

      // Handle 'endPosition' with null check
      Cell endPosCell = dataRow.createCell(8);
      if (record.endPosition() != null) {
        endPosCell.setCellValue(record.endPosition());
      } else {
        endPosCell.setCellValue("");
      }

      dataRow.createCell(9).setCellValue(record.issueType());
      dataRow.createCell(10).setCellValue(record.issueDescription());
      dataRow.createCell(11).setCellValue(record.issueLevel());
      dataRow.createCell(12).setCellValue(record.repairSuggestion());
      dataRow.createCell(13).setCellValue(record.evaluationDate().toString());
      dataRow.createCell(14).setCellValue(record.issueCreator());
      dataRow.createCell(15).setCellValue(record.hasFixed());
      dataRow.createCell(16).setCellValue(record.appliedToHub());
      dataRow.createCell(17).setCellValue(record.comments());
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

  private String truncateValue(String value) {
    if (value != null && value.length() > MAX_CELL_LENGTH) {
      return value.substring(0, MAX_CELL_LENGTH);
    }
    return value;
  }
}
