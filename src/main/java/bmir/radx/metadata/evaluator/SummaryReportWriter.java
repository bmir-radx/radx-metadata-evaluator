package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.result.ValidationResult;
import bmir.radx.metadata.evaluator.util.StudyPhsGetter;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static bmir.radx.metadata.evaluator.MetadataEntity.*;
import static bmir.radx.metadata.evaluator.statistics.SummaryReportCalculator.groupByStudyPhsAndEntities;


@Component
public class SummaryReportWriter {
  private final String SHEET_NAME = "RADx Metadata Evaluation Report";
  private final String STUDY_DESCRIPTION = "Study Description";

  private final StudyPhsGetter studyPhsGetter;

  public SummaryReportWriter(StudyPhsGetter studyPhsGetter) {
    this.studyPhsGetter = studyPhsGetter;
  }

  /***
   * {
   *   "study phs992": {
   *     "study": {
   *       "issueType1": ["uuid1", "uuid2"],
   *       "issueType2": ["uuid4"]
   *     },
   *     "data file": {
   *       "issueType1": ["uuid3"]
   *     }
   *   },
   *   "study phs304": {
   *     ...
   *   }
   * }
   */
  public void writeSummaryReport(Path study, Path datafile, Workbook workbook, Map<MetadataEntity, EvaluationReport<? extends ValidationResult>> reports) throws IOException {
    Sheet sheet = workbook.createSheet(SHEET_NAME);

    var data = groupByStudyPhsAndEntities(reports);
    var studyPhsPool = studyPhsGetter.getStudyPhsPool(study, datafile);
    Map<MetadataEntity, Set<String>> issueTypes =  new HashMap<>();
    for(var entity: MetadataEntity.values()){
      issueTypes.put(entity, getIssueTypes(data, entity));
    }

    Map<String, Map<String, List<String>>> studyDescriptionIssuesData = new HashMap<>();
    Map<String, List<String>> sd = new HashMap<>();
    sd.put("Lack of Structure", List.of("Yes"));
    sd.put("Issue 1", List.of("123", "234"));
    sd.put("Issue 2", List.of("234", "234"));
    studyDescriptionIssuesData.put("phs003032", sd);
    var studyDescriptionIssueTypes = getStudyDescriptionIssueTypes(studyDescriptionIssuesData);

    writeHeaders(sheet, issueTypes, studyDescriptionIssueTypes);
    writeValues(sheet, data, studyPhsPool, issueTypes, studyDescriptionIssuesData, studyDescriptionIssueTypes);
  }

  private Set<String> getIssueTypes(Map<String, Map<MetadataEntity, Map<String, List<String>>>> data, MetadataEntity entity) {
    Set<String> issueTypes = new LinkedHashSet<>();
    for (var entityData : data.values()) {
      if (entityData.containsKey(entity)) {
        issueTypes.addAll(entityData.get(entity).keySet());
      }
    }
    return issueTypes;
  }

  private Set<String> getStudyDescriptionIssueTypes(Map<String, Map<String, List<String>>> studyDescriptionIssueTypes) {
    Set<String> issueTypes = new LinkedHashSet<>();
    for (var studyData : studyDescriptionIssueTypes.values()) {
      issueTypes.addAll(studyData.keySet());
    }
    return issueTypes;
  }

  private void writeHeaders(Sheet sheet, Map<MetadataEntity, Set<String>> issueTypes, Set<String> studyDescriptionIssues) {
    // Create headers
    Row entityRow = sheet.createRow(0);
    Row subEntityRow = sheet.createRow(1); // Leave this row empty for now
    Row issueTypeRow = sheet.createRow(2);

    //Create bold style
    var boldStyle = createBoldStyle(sheet);

    // Set column headers
    int columnIndex = 1; // Start from column B
    Map<String, int[]> mergeRegions = new LinkedHashMap<>(); // Track ranges to merge

    // Write headers for "Study"
    if (issueTypes.containsKey(STUDY_METADATA)) {
      columnIndex = writeHeader(columnIndex, STUDY_METADATA, entityRow, issueTypeRow, boldStyle, issueTypes, mergeRegions);
    }

    // Write headers for "Study Description"
    columnIndex = writeStudyDescriptionHeader(columnIndex, issueTypes.containsKey(STUDY_METADATA), entityRow, subEntityRow, issueTypeRow, boldStyle, studyDescriptionIssues, mergeRegions);

    // Write headers for other entities like "Data File" and "Variable"
    for (var entity : MetadataEntity.values()) {
      if (entity == STUDY_METADATA) continue; // Already handled above
      columnIndex = writeHeader(columnIndex, entity, entityRow, issueTypeRow, boldStyle, issueTypes, mergeRegions);
    }

    // Write "Total Issues" column
    var totalIssuesCell = entityRow.createCell(columnIndex);
    totalIssuesCell.setCellValue("Total Issues");
    totalIssuesCell.setCellStyle(boldStyle);
    mergeRegions.put("Total Issues", new int[]{columnIndex, columnIndex});

    //Merge header cells
    mergeHeaderCells(mergeRegions, sheet);
  }

  private void writeStudyDescriptionValues(
      Row row,
      Map<String, List<String>> studyDescriptionData,
      Set<String> studyDescriptionIssues,
      int startColumnIndex,
      int totalIssuesCount
  ) {
    int columnIndex = startColumnIndex;
    for (String issueType : studyDescriptionIssues) {
      Cell cell = row.createCell(columnIndex++);
      List<String> uuids = studyDescriptionData.getOrDefault(issueType, Collections.emptyList());
      cell.setCellValue(String.join(", ", uuids));
      totalIssuesCount += uuids.size();
    }
  }

  private void writeValues(Sheet sheet,
                           Map<String, Map<MetadataEntity, Map<String, List<String>>>> data,
                           Set<String> studyPhsPool,
                           Map<MetadataEntity, Set<String>> issueTypes,
                           Map<String, Map<String, List<String>>> studyDescriptionIssuesData,
                           Set<String> studyDescriptionIssueTypes) {
    int rowIndex = 3; // Start from the fourth row
    for(var studyPhs: studyPhsPool){
      Row row = sheet.createRow(rowIndex++);
      Cell studyPhsCell = row.createCell(0);
      studyPhsCell.setCellValue(studyPhs);
      String url = "https://radxdatahub.nih.gov/study/58";
      addHyperlinkToCell(studyPhsCell, url, sheet.getWorkbook());
      int totalIssuesCount = 1; //All study description lack of structure
      int columnIndex = 1;

      if(data.containsKey(studyPhs)){
        var entityData = data.get(studyPhs);

        // Write UUIDs for "Study"
        if (entityData.containsKey(STUDY_METADATA)) {
          Map<String, List<String>> studyIssues = entityData.get(STUDY_METADATA);
          for (String issueType : issueTypes.get(STUDY_METADATA)) {
            Cell cell = row.createCell(columnIndex++);
            List<String> uuids = studyIssues.getOrDefault(issueType, Collections.emptyList());
            cell.setCellValue(String.join(", ", uuids));
            totalIssuesCount += uuids.size();
          }
        } else {
          columnIndex += issueTypes.get(STUDY_METADATA).size();
        }

        // Write UUIDs for "Study Description"
        Map<String, List<String>> studyDescriptionData = studyDescriptionIssuesData.getOrDefault(studyPhs, Collections.emptyMap());
        writeStudyDescriptionValues(row, studyDescriptionData, studyDescriptionIssueTypes, columnIndex, totalIssuesCount);
        columnIndex += studyDescriptionIssueTypes.size();

        // Write UUIDs for other entities like "Data File"
        for (var entity : MetadataEntity.values()) {
          if (entity == STUDY_METADATA) continue; // Already handled above
          var currentIssueTypes = issueTypes.get(entity);
          if (entityData.containsKey(entity)) {
            Map<String, List<String>> entityIssues = entityData.get(entity);
            for (String issueType : currentIssueTypes) {
              Cell cell = row.createCell(columnIndex++);
              List<String> uuids = entityIssues.getOrDefault(issueType, Collections.emptyList());
              cell.setCellValue(String.join(", ", uuids));
              totalIssuesCount += uuids.size();
            }
          } else {
            columnIndex += currentIssueTypes.size();
          }
        }
      } else{
        columnIndex += getAllIssueTypesCount(issueTypes, studyDescriptionIssueTypes);
      }

      //Write total issues count
      Cell totalIssuesCell = row.createCell(columnIndex);
      totalIssuesCell.setCellValue(totalIssuesCount);
    }
  }

  private int writeHeader(int columnIndex, MetadataEntity entity, Row entityRow, Row issueTypeRow, CellStyle boldStyle, Map<MetadataEntity, Set<String>> issueTypes, Map<String, int[]> mergeRegions){
    var currentIssueTypes = issueTypes.get(entity);
    int startColumn = columnIndex;
    for (String issueType : currentIssueTypes) {
      var entityCell = entityRow.createCell(columnIndex);
      entityCell.setCellValue(entity.getEntityName());
      entityCell.setCellStyle(boldStyle);
      var issueTypeCell = issueTypeRow.createCell(columnIndex);
      issueTypeCell.setCellValue(issueType);
      issueTypeCell.setCellStyle(boldStyle);
      columnIndex++;
    }
    if (currentIssueTypes.size() > 0) {
      mergeRegions.put(entity.getEntityName(), new int[]{startColumn, columnIndex - 1});
    }
    return columnIndex;
  }

  private int writeStudyDescriptionHeader(int columnIndex,
                                          boolean containStudyMetadata,
                                          Row entityRow,
                                          Row subEntityRow,
                                          Row issueTypeRow,
                                          CellStyle boldStyle,
                                          Set<String> studyDescriptionIssues,
                                          Map<String, int[]> mergeRegions){
    int studyDescStartColumn = columnIndex;
    for (String issueType : studyDescriptionIssues) {
      var entityCell = entityRow.createCell(columnIndex);
      entityCell.setCellValue(STUDY_METADATA.getEntityName());
      var subEntityCell = subEntityRow.createCell(columnIndex);
      subEntityCell.setCellValue(STUDY_DESCRIPTION);
      subEntityCell.setCellStyle(boldStyle);
      var issueTypeCell = issueTypeRow.createCell(columnIndex);
      issueTypeCell.setCellValue(issueType);
      issueTypeCell.setCellStyle(boldStyle);
      columnIndex++;
    }
    if (!studyDescriptionIssues.isEmpty()) {
      mergeRegions.put(STUDY_DESCRIPTION, new int[]{studyDescStartColumn, columnIndex - 1});
    }

    // Merge "Study Metadata" across both "Study Metadata" and "Study Description" sub-entities
    if (containStudyMetadata) {
      mergeRegions.put(STUDY_METADATA.getEntityName(), new int[]{mergeRegions.get(STUDY_METADATA.getEntityName())[0], columnIndex - 1});
    }
    return columnIndex;
  }

  private void mergeHeaderCells(Map<String, int[]> mergeRegions, Sheet sheet){
    // Merge header cells
    for (var entry : mergeRegions.entrySet()) {
      String entity = entry.getKey();
      int[] range = entry.getValue();
      if (range[0] == range[1]) {
        // Single-column, merge all three rows
        sheet.addMergedRegion(new CellRangeAddress(0, 2, range[0], range[1]));
      } else if (entity.equals(STUDY_DESCRIPTION)) {
        sheet.addMergedRegion(new CellRangeAddress(1, 1, range[0], range[1]));
      } else{
        sheet.addMergedRegion(new CellRangeAddress(0, 0, range[0], range[1]));
      }
    }
  }

  private CellStyle createBoldStyle(Sheet sheet){
    CellStyle boldStyle = sheet.getWorkbook().createCellStyle();
    Font boldFont = sheet.getWorkbook().createFont();
    boldFont.setBold(true);
    boldStyle.setFont(boldFont);
    return boldStyle;
  }

  private int getAllIssueTypesCount(Map<MetadataEntity, Set<String>> issueTypes, Set<String> studyDescriptionIssues){
    int count = 0;
    for(var entry: issueTypes.entrySet()){
      count += entry.getValue().size();
    }

    return count + studyDescriptionIssues.size();
  }

  private void addHyperlinkToCell(Cell cell, String url, Workbook workbook) {
    // Create a CreationHelper
    CreationHelper creationHelper = workbook.getCreationHelper();

    // Create the Hyperlink
    Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
    hyperlink.setAddress(url);

    // Assign the Hyperlink to the Cell
    cell.setHyperlink(hyperlink);

    // Create and Apply a Hyperlink Style
    CellStyle hyperlinkStyle = workbook.createCellStyle();
    Font hyperlinkFont = workbook.createFont();
    hyperlinkFont.setUnderline(Font.U_SINGLE); // Underline
    hyperlinkFont.setColor(IndexedColors.BLUE.getIndex()); // Blue color
    hyperlinkStyle.setFont(hyperlinkFont);
    cell.setCellStyle(hyperlinkStyle);
  }

}
