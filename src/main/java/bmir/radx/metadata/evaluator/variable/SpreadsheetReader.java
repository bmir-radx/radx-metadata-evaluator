package bmir.radx.metadata.evaluator.variable;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static bmir.radx.metadata.evaluator.variable.HeaderName.*;

@Component
public class SpreadsheetReader {
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
    int rowNumber = row.getRowNum() + 1; // Adjust row number to exclude header row
    String concept = getCellValueAsString(row.getCell(headerMap.get(HeaderName.CONCEPT)));
    String radxGlobalPrompt = getCellValueAsString(row.getCell(headerMap.get(HeaderName.RADX_GLOBAL_PROMPT)));
    String variable = getCellValueAsString(row.getCell(headerMap.get(HeaderName.VARIABLE)));
    String responses = getCellValueAsString(row.getCell(headerMap.get(HeaderName.RESPONSES)));

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

  private static boolean isRowEmpty(Row row, Map<HeaderName, Integer> headerMap) {
    return headerMap.values().stream()
        .map(row::getCell)
        .allMatch(cell -> cell == null || cell.getCellType() == CellType.BLANK);
  }

}
