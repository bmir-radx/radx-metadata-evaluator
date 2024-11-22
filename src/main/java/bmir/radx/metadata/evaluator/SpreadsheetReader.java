package bmir.radx.metadata.evaluator;

import bmir.radx.metadata.evaluator.study.StudyMetadataRow;
import bmir.radx.metadata.evaluator.variable.AllVariablesRow;
import bmir.radx.metadata.evaluator.variable.GlobalCodeBookRow;
import bmir.radx.metadata.evaluator.variable.VariableMetadataRow;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static bmir.radx.metadata.evaluator.HeaderName.*;

@Component
public class SpreadsheetReader {
  @Value("${radx.bundles.mapping.file.name}")
  private String radxBundlesFileName;

  public List<VariableMetadataRow> readVariablesMetadata(Path filePath) throws IOException {
    try (var fileInputStream = new FileInputStream(filePath.toFile());
         var workbook = WorkbookFactory.create(fileInputStream)) {
      var sheet = workbook.getSheetAt(0);
      Map<HeaderName, Integer> headerMap = getHeaderMap(sheet.getRow(0));

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
      Map<HeaderName, Integer> headerMap = getHeaderMap(sheet.getRow(0));

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
      Map<HeaderName, Integer> headerMap = getHeaderMap(sheet.getRow(0));

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
      Map<HeaderName, Integer> headerMap = getHeaderMap(sheet.getRow(0));

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
      Map<HeaderName, Integer> headerMap = getHeaderMap(sheet.getRow(0));

      // Find the column index for "STUDY PHS"
      int studyPhsColumnIndex = headerMap.getOrDefault(HeaderName.STUDY_PHS, -1);
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

  private Map<HeaderName, Integer> getHeaderMap(Row headerRow) {
    Map<HeaderName, Integer> headerMap = new EnumMap<>(HeaderName.class);
    for (Cell cell : headerRow) {
      String headerName = cell.getStringCellValue();
      if(!headerName.isEmpty()){
        var field = HeaderName.fromHeaderName(headerName);
        headerMap.put(field, cell.getColumnIndex());
      }
    }
    return headerMap;
  }

  private VariableMetadataRow mapRowToVariableMetadata(Row row, Map<HeaderName, Integer> headerMap) {
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

  private GlobalCodeBookRow mapRowToGlobalCodeBook(Row row, Map<HeaderName, Integer> headerMap) {
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

  private AllVariablesRow mapRowToAllVariables(Row row, Map<HeaderName, Integer> headerMap) {
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

  private StudyMetadataRow mapRowToStudiesMetadata(Row row, Map<HeaderName, Integer> headerMap){
    return new StudyMetadataRow(
        getRowNumber(row),
        getStringCellValue(row, headerMap, STUDY_PROGRAM),
        getStringCellValue(row, headerMap, STUDY_PHS),
        getStringCellValue(row, headerMap, STUDY_TITLE),
        getStringCellValue(row, headerMap, DESCRIPTION),
        getStringCellValue(row, headerMap, RADX_ACKNOWLEDGEMENTS),
        getStringCellValue(row, headerMap, NIH_GRANT_NUMBER),
        getStringCellValue(row, headerMap, RAPIDS_LINK),
        getDateCellValue(row, headerMap, STUDY_START_DATE),
        getDateCellValue(row, headerMap, STUDY_END_DATE),
        getDateCellValue(row, headerMap, STUDY_RELEASE_DATE),
        getDateCellValue(row, headerMap, UPDATED_AT),
        getStringCellValue(row, headerMap, FOA_NUMBER),
        getStringCellValue(row, headerMap, FOA_URL),
        getStringCellValue(row, headerMap, CONTACT_PI_PROJECT_LEADER),
        getStringCellValue(row, headerMap, STUDY_DOI),
        getStringCellValue(row, headerMap, DCC_PROVIDED_PUBLICATION_URLS),
        getStringCellValue(row, headerMap, CLINICALTRIALS_GOV_URL),
        getStringCellValue(row, headerMap, STUDY_WEBSITE_URL),
        getStringCellValue(row, headerMap, STUDY_DESIGN),
        getStringCellValue(row, headerMap, DATA_TYPES),
        getStringCellValue(row, headerMap, STUDY_DOMAIN),
        getStringCellValue(row, headerMap, NIH_INSTITUTE_OR_CENTER),
        getBooleanCellValue(row, headerMap, MULTI_CENTER_STUDY, "TRUE"),
        getStringCellValue(row, headerMap, MULTI_CENTER_SITES),
        getStringCellValue(row, headerMap, KEYWORDS),
        getStringCellValue(row, headerMap, DATA_COLLECTION_METHOD),
        getIntegerCellValue(row, headerMap, ESTIMATED_COHORT_SIZE),
        getStringCellValue(row, headerMap, STUDY_POPULATION_FOCUS),
        getStringCellValue(row, headerMap, SPECIES),
        getStringCellValue(row, headerMap, CONSENT_DATA_USE_LIMITATIONS),
        getStringCellValue(row, headerMap, STUDY_STATUS),
        getBooleanCellValue(row, headerMap, HAS_DATA_FILES, "Yes")
    );
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null) {
      return null;
    }
    String cellValue = switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue();
      case NUMERIC -> Double.toString(cell.getNumericCellValue());
      case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
      case FORMULA -> cell.getCellFormula();
      default -> null;
    };
    return "null".equals(cellValue) ? null : cellValue;
  }

  private int getRowNumber(Row row){
    return row.getRowNum() + 1;
  }

  private static boolean isRowEmpty(Row row, Map<HeaderName, Integer> headerMap) {
    return headerMap.values().stream()
        .map(row::getCell)
        .allMatch(cell -> cell == null || cell.getCellType() == CellType.BLANK);
  }

  private String getStringCellValue(Row row, Map<HeaderName, Integer> headerMap, HeaderName header) {
    Integer columnIndex = headerMap.get(header);
    if (columnIndex == null) return null;
    Cell cell = row.getCell(columnIndex);
    return cell != null ? cell.getStringCellValue() : null;
  }

  private Date getDateCellValue(Row row, Map<HeaderName, Integer> headerMap, HeaderName header) {
    Integer columnIndex = headerMap.get(header);
    if (columnIndex == null) return null;
    Cell cell = row.getCell(columnIndex);
    return cell != null ? cell.getDateCellValue() : null;
  }

  private Integer getIntegerCellValue(Row row, Map<HeaderName, Integer> headerMap, HeaderName header) {
    Integer columnIndex = headerMap.get(header);
    if (columnIndex == null) return null;
    Cell cell = row.getCell(columnIndex);
    return cell != null ? (int) cell.getNumericCellValue() : null;
  }

  private Boolean getBooleanCellValue(Row row, Map<HeaderName, Integer> headerMap, HeaderName header, String trueValue) {
    Integer columnIndex = headerMap.get(header);
    if (columnIndex == null) return null;
    Cell cell = row.getCell(columnIndex);
    if (cell == null) return null;
    String value = cell.getStringCellValue();
    return trueValue.equalsIgnoreCase(value);
  }
}
