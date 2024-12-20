package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.study.StudyMetadataRow;
import bmir.radx.metadata.evaluator.util.StudyHeaderConverter;
import bmir.radx.metadata.evaluator.variable.AllVariablesRow;
import bmir.radx.metadata.evaluator.variable.GlobalCodeBookRow;
import bmir.radx.metadata.evaluator.variable.VariableMetadataRow;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static bmir.radx.metadata.evaluator.SpreadsheetHeaders.*;

@Component
public class SpreadsheetReader {
  @Value("${radx.bundles.mapping.file.name}")
  private String radxBundlesFileName;

  @Value("${radx.study.code.list.file.name}")
  private String radxCodeListFileName;

  public List<VariableMetadataRow> readVariablesMetadata(Path filePath) throws IOException {
    try (var fileInputStream = new FileInputStream(filePath.toFile());
         var workbook = WorkbookFactory.create(fileInputStream)) {
      var sheet = workbook.getSheetAt(0);
      Map<SpreadsheetHeaders, Integer> headerMap = getVariableHeaderMap(sheet.getRow(0));

      return StreamSupport.stream(sheet.spliterator(), false)
          .skip(1) // Skip header row
          .filter(row -> !isRowEmpty(row, headerMap))
          .map(row -> mapRowToVariableMetadata(row, headerMap))
          .collect(Collectors.toList());
    }
  }

  public List<GlobalCodeBookRow> readGlobalCodeBook(InputStream inputStream) throws IOException{
    try (Workbook workbook = WorkbookFactory.create(inputStream)) {
      Sheet sheet = workbook.getSheetAt(0);
      Map<SpreadsheetHeaders, Integer> headerMap = getVariableHeaderMap(sheet.getRow(0));

      return StreamSupport.stream(sheet.spliterator(), false)
          .skip(1) // Skip header row
          .filter(row -> !isRowEmpty(row, headerMap))
          .map(row -> mapRowToGlobalCodeBook(row, headerMap))
          .collect(Collectors.toList());
    }
  }

  public List<AllVariablesRow> readAllVariables(Path filePath) throws IOException {
    try (var fileInputStream = new FileInputStream(filePath.toFile());
         var workbook = WorkbookFactory.create(fileInputStream)) {
      var sheet = workbook.getSheetAt(1);
      Map<SpreadsheetHeaders, Integer> headerMap = getVariableHeaderMap(sheet.getRow(0));

      return StreamSupport.stream(sheet.spliterator(), false)
          .skip(1) // Skip header row
          .filter(row -> !isRowEmpty(row, headerMap))
          .map(row -> mapRowToAllVariables(row, headerMap))
          .collect(Collectors.toList());
    }
  }

  public List<StudyMetadataRow> readStudyMetadata(Path filePath) throws IOException {
    try (var fileInputStream = new FileInputStream(filePath.toFile());
         var workbook = WorkbookFactory.create(fileInputStream)) {
      var sheet = workbook.getSheetAt(0);
      Map<StudyTemplateFields, Integer> headerMap = getStudyHeaderMap(sheet.getRow(0));

      return StreamSupport.stream(sheet.spliterator(), false)
          .skip(1) // Skip header row
          .filter(row -> !isRowEmpty(row, headerMap))
          .map(row -> mapRowToStudiesMetadata(row, headerMap))
          .collect(Collectors.toList());
    }
  }

  /***
   * This method return the mappings of study PHS to StudyMetadataRow instance
   */
  public Map<String, StudyMetadataRow> getStudyMetadataMapping(Path filePath){
    try (var fileInputStream = new FileInputStream(filePath.toFile());
         var workbook = WorkbookFactory.create(fileInputStream)) {
      var sheet = workbook.getSheetAt(0);
      Map<StudyTemplateFields, Integer> headerMap = getStudyHeaderMap(sheet.getRow(0));

      // Find the column index for "STUDY PHS"
      int studyPhsColumnIndex = headerMap.getOrDefault(StudyTemplateFields.STUDY_PHS, -1);
      if (studyPhsColumnIndex == -1) {
        throw new IllegalArgumentException("STUDY PHS column is missing in the sheet.");
      }

      return StreamSupport.stream(sheet.spliterator(), false)
          .skip(1)
          .filter(row -> !isRowEmpty(row, headerMap))
          .collect(Collectors.toMap(
              row -> getCellValueAsString(row.getCell(studyPhsColumnIndex)), // Key: STUDY PHS value
              row -> mapRowToStudiesMetadata(row, headerMap),               // Value: Mapped StudyMetadataRow
              (existing, replacement) -> {
                // Print an error message for duplicates
                System.err.println("Duplicate STUDY PHS key found at row " + replacement.rowNumber());
                return existing;
              }
          ));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /***
   * This method read RADx bundles spreadsheet and return data file name to study phs mappings
   */
  public Map<String, String> getDataFile2StudyMapping(){
    Map<String, String> resultMap = new HashMap<>();

    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(radxBundlesFileName);
         Workbook workbook = WorkbookFactory.create(inputStream)) {

      Sheet sheet = workbook.getSheetAt(0);

      for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
        Row row = sheet.getRow(rowIndex);

        if (row == null) continue;

        Cell phsCell = row.getCell(1);
        Cell transformMetaCell = row.getCell(4);
        Cell origMetaCell = row.getCell(7);

        String phsValue = getCellValueAsString(phsCell);
        String transformMetaValue = getCellValueAsString(transformMetaCell);
        String origMetaValue = getCellValueAsString(origMetaCell);

        if (!transformMetaValue.isEmpty() && !phsValue.isEmpty()) {
          resultMap.put(transformMetaValue, phsValue);
        }
        if (!origMetaValue.isEmpty() && !phsValue.isEmpty()) {
          resultMap.put(origMetaValue, phsValue);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return resultMap;
  }

  public Map<String, Set<String>> readCodeListValues(String sheetName, int column){
    Map<String, Set<String>> codeListValues = new HashMap<>();

    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(radxCodeListFileName)) {
      assert inputStream != null;
      try (Workbook workbook = WorkbookFactory.create(inputStream)) {

        Sheet sheet = workbook.getSheet(sheetName);
        if (sheet == null) {
          throw new IllegalArgumentException("Sheet " + sheetName + " does not exist in the file.");
        }

        for (Row row : sheet) {
          if (row.getRowNum() == 0) continue;

          Cell keyCell = row.getCell(0);
          Cell valueCell = row.getCell(column); // Column B

          if (keyCell == null || valueCell == null) continue;

          String key = getCellValueAsString(keyCell);
          var fieldName = StudyHeaderConverter.convertCodeListHeaderToField(key);
          String value = getCellValueAsString(valueCell);

          if (key != null && value != null) {
            codeListValues.computeIfAbsent(fieldName, k -> new HashSet<>()).add(value.trim());
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return codeListValues;
  }

  private Map<SpreadsheetHeaders, Integer> getVariableHeaderMap(Row headerRow) {
    Map<SpreadsheetHeaders, Integer> headerMap = new EnumMap<>(SpreadsheetHeaders.class);
    for (Cell cell : headerRow) {
      String headerName = cell.getStringCellValue();
      if(!headerName.isEmpty()){
        var field = SpreadsheetHeaders.fromHeaderName(headerName);
        headerMap.put(field, cell.getColumnIndex());
      }
    }
    return headerMap;
  }

  private Map<StudyTemplateFields, Integer> getStudyHeaderMap(Row headerRow) {
    Map<StudyTemplateFields, Integer> headerMap = new EnumMap<>(StudyTemplateFields.class);
    for (Cell cell : headerRow) {
      String headerName = cell.getStringCellValue();
      if(!headerName.isEmpty()){
        var field = StudyTemplateFields.fromHeaderName(headerName);
        headerMap.put(field, cell.getColumnIndex());
      }
    }
    return headerMap;
  }


  private VariableMetadataRow mapRowToVariableMetadata(Row row, Map<SpreadsheetHeaders, Integer> headerMap) {
    int rowNumber = row.getRowNum() + 1;
    String dataVariable = getCellValueAsString(row.getCell(headerMap.get(DATA_VARIABLE)));
    boolean isTier1CDE = Boolean.parseBoolean(getCellValueAsString(row.getCell(headerMap.get(IS_TIER_1_CDE))));
    int fileCount = (int)Double.parseDouble(getCellValueAsString(row.getCell(headerMap.get(FILE_COUNT))));
    int studyCount = (int)Double.parseDouble(getCellValueAsString(row.getCell(headerMap.get(STUDY_COUNT))));
    List<String> dbGaPIDs = Arrays.stream(getCellValueAsString(row.getCell(headerMap.get(DB_GAP_IDS))).split(","))
        .map(String::trim)
        .collect(Collectors.toList());
    List<String> filesPerStudy = Arrays.stream(getCellValueAsString(row.getCell(headerMap.get(FILES_PER_STUDY))).split(";"))
        .map(String::trim)
        .collect(Collectors.toList());
    List<String> radxProgram = Arrays.stream(getCellValueAsString(row.getCell(headerMap.get(RADX_PROGRAM))).split(","))
        .map(String::trim)
        .collect(Collectors.toList());
    String label = getCellValueAsString(row.getCell(headerMap.get(LABEL)));
    String concept = getCellValueAsString(row.getCell(headerMap.get(CONCEPT)));
    String responses = getCellValueAsString(row.getCell(headerMap.get(RESPONSES)));
    String radxGlobalPrompt = getCellValueAsString(row.getCell(headerMap.get(RADX_GLOBAL_PROMPT)));

    return new VariableMetadataRow(
        rowNumber,
        dataVariable,
        isTier1CDE,
        fileCount,
        studyCount,
        dbGaPIDs,
        filesPerStudy,
        radxProgram,
        label,
        concept,
        responses,
        radxGlobalPrompt
    );
  }

  private GlobalCodeBookRow mapRowToGlobalCodeBook(Row row, Map<SpreadsheetHeaders, Integer> headerMap) {
    int rowNumber = getRowNumber(row);
    String concept = getCellValueAsString(row.getCell(headerMap.get(CONCEPT)));
    String radxGlobalPrompt = getCellValueAsString(row.getCell(headerMap.get(RADX_GLOBAL_PROMPT)));
    String variable = getCellValueAsString(row.getCell(headerMap.get(VARIABLE)));
    String responses = getCellValueAsString(row.getCell(headerMap.get(RESPONSES)));

    return new GlobalCodeBookRow(
        rowNumber,
        concept,
        radxGlobalPrompt,
        variable,
        responses
    );
  }

  private AllVariablesRow mapRowToAllVariables(Row row, Map<SpreadsheetHeaders, Integer> headerMap) {
    String radxProgram = getCellValueAsString(row.getCell(headerMap.get(RADX_PROGRAM)));
    String studyName = getCellValueAsString(row.getCell(headerMap.get(STUDY_NAME)));
    String phsId = getCellValueAsString(row.getCell(headerMap.get(DB_GAP_ID)));
    String fileName = getCellValueAsString(row.getCell(headerMap.get(FILE_NAME)));
    List<String> variables = Arrays.stream(getCellValueAsString(row.getCell(headerMap.get(VARIABLES))).split(","))
        .map(String::trim)
        .toList();

    return new AllVariablesRow(
        radxProgram,
        studyName,
        phsId,
        fileName,
        variables
    );
  }

  private StudyMetadataRow mapRowToStudiesMetadata(Row row, Map<StudyTemplateFields, Integer> headerMap){
    return new StudyMetadataRow(
        getRowNumber(row),
        getStringCellValue(row, headerMap, StudyTemplateFields.STUDY_PROGRAM),
        getStringCellValue(row, headerMap, StudyTemplateFields.STUDY_PHS),
        getStringCellValue(row, headerMap, StudyTemplateFields.STUDY_TITLE),
        getStringCellValue(row, headerMap, StudyTemplateFields.DESCRIPTION),
        getStringCellValue(row, headerMap, StudyTemplateFields.RADX_ACKNOWLEDGEMENTS),
        getStringCellValue(row, headerMap, StudyTemplateFields.NIH_GRANT_NUMBERS),
//        getStringCellValue(row, headerMap, RAPIDS_LINK),
        getDateCellValue(row, headerMap, StudyTemplateFields.STUDY_START_DATE),
        getDateCellValue(row, headerMap, StudyTemplateFields.STUDY_END_DATE),
        getDateCellValue(row, headerMap, StudyTemplateFields.STUDY_RELEASE_DATE),
        getDateCellValue(row, headerMap, StudyTemplateFields.UPDATED_DATE),
        getStringCellValue(row, headerMap, StudyTemplateFields.FOA_NUMBERS),
        getStringCellValue(row, headerMap, StudyTemplateFields.FOA_URLS),
        getStringCellValue(row, headerMap, StudyTemplateFields.PRINCIPAL_INVESTIGATOR),
        getStringCellValue(row, headerMap, StudyTemplateFields.STUDY_DOI),
        getStringCellValue(row, headerMap, StudyTemplateFields.PUBLICATION_URLS),
        getStringCellValue(row, headerMap, StudyTemplateFields.CLINICALTRIALS_GOV_URLS),
        getStringCellValue(row, headerMap, StudyTemplateFields.STUDY_WEBSITE_URLS),
        getStringCellValue(row, headerMap, StudyTemplateFields.STUDY_DESIGN),
        getStringCellValue(row, headerMap, StudyTemplateFields.DATA_TYPES),
        getStringCellValue(row, headerMap, StudyTemplateFields.STUDY_DOMAINS),
        getStringCellValue(row, headerMap, StudyTemplateFields.NIH_INSTITUTES_OR_CENTERS),
        getStringCellValue(row, headerMap, StudyTemplateFields.MULTI_CENTER_STUDY),
        getStringCellValue(row, headerMap, StudyTemplateFields.STUDY_SITES),
        getStringCellValue(row, headerMap, StudyTemplateFields.KEYWORDS),
        getStringCellValue(row, headerMap, StudyTemplateFields.DATA_COLLECTION_METHODS),
        getIntegerCellValue(row, headerMap, StudyTemplateFields.ESTIMATED_SAMPLE_SIZE),
        getStringCellValue(row, headerMap, StudyTemplateFields.STUDY_POPULATION_FOCUS),
        getStringCellValue(row, headerMap, StudyTemplateFields.SPECIES),
        getStringCellValue(row, headerMap, StudyTemplateFields.CONSENT_OR_DATA_USE_LIMITATIONS),
        getStringCellValue(row, headerMap, StudyTemplateFields.STUDY_STATUS),
        getStringCellValue(row, headerMap, StudyTemplateFields.HAS_DATA_FILES),
        getStringCellValue(row, headerMap, StudyTemplateFields.DISEASE_SPECIFIC_GROUP),
        getStringCellValue(row, headerMap, StudyTemplateFields.DISEASE_SPECIFIC_RELATED_CONDITIONS),
        getStringCellValue(row, headerMap, StudyTemplateFields.HEALTH_BIOMED_GROUP),
        getStringCellValue(row, headerMap, StudyTemplateFields.CITATION),
        getNumericCellValue(row, headerMap, StudyTemplateFields.STUDY_SIZE),
        getStringCellValue(row, headerMap, StudyTemplateFields.VERSION_NUMBER),
        getStringCellValue(row, headerMap, StudyTemplateFields.COHORT_SIZE_RANGE),
        getDateCellValue(row, headerMap, StudyTemplateFields.CREATION_DATE)
    );
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null) {
      return null;
    }
    String cellValue = switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue();
      case NUMERIC -> Double.toString(cell.getNumericCellValue());
      case BOOLEAN -> cell.getBooleanCellValue() ? "TRUE" : "FALSE"; // Ensure uppercase
      case FORMULA -> cell.getCellFormula();
      default -> null;
    };
    return "null".equals(cellValue) ? null : cellValue;
  }

  private int getRowNumber(Row row){
    return row.getRowNum() + 1;
  }

  private static boolean isRowEmpty(Row row, Map<? extends Header, Integer> headerMap) {
    return headerMap.values().stream()
        .map(row::getCell)
        .allMatch(cell -> cell == null || cell.getCellType() == CellType.BLANK);
  }

  private String getStringCellValue(Row row, Map<? extends Header, Integer> headerMap, StudyTemplateFields header) {
    Integer columnIndex = headerMap.get(header);
    if (columnIndex == null) return null;
    Cell cell = row.getCell(columnIndex);
    return cell != null ? cell.getStringCellValue() : null;
  }

  private Date getDateCellValue(Row row, Map<? extends Header, Integer> headerMap, StudyTemplateFields header) {
    Integer columnIndex = headerMap.get(header);
    if (columnIndex == null) return null;
    Cell cell = row.getCell(columnIndex);
    return cell != null ? cell.getDateCellValue() : null;
  }

  private Integer getIntegerCellValue(Row row, Map<? extends Header, Integer> headerMap, StudyTemplateFields header) {
    Integer columnIndex = headerMap.get(header);
    if (columnIndex == null) return null;
    Cell cell = row.getCell(columnIndex);
    return cell != null ? (int) cell.getNumericCellValue() : null;
  }

  private Boolean getBooleanCellValue(Row row, Map<? extends Header, Integer> headerMap, StudyTemplateFields header, String trueValue) {
    Integer columnIndex = headerMap.get(header);
    if (columnIndex == null) return null;
    Cell cell = row.getCell(columnIndex);
    if (cell == null) return null;
    String value = cell.getStringCellValue();
    return trueValue.equalsIgnoreCase(value);
  }

  private Double getNumericCellValue(Row row, Map<? extends Header, Integer> headerMap, StudyTemplateFields header) {
    Integer columnIndex = headerMap.get(header);
    if (columnIndex == null) return null;
    Cell cell = row.getCell(columnIndex);
    if (cell == null) return null;

    // Check if the cell is numeric
    if (cell.getCellType() == CellType.NUMERIC) {
      return cell.getNumericCellValue();
    } else {
      System.err.println("Expected numeric value at row " + (row.getRowNum() + 1) + ", column " + columnIndex);
      return Double.NaN;
    }
  }
}
