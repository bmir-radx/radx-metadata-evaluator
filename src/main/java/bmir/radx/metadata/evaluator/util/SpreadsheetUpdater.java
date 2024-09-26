package bmir.radx.metadata.evaluator.util;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

@Component
public class SpreadsheetUpdater {
  private final String schemaTitle = "schema:title";
  private final String pavVersion = "pav:version";
  private final String pavCreatedOn = "pav:createdOn";
  private final String pavDerivedFrom = "pav:derivedFrom";
  private final String metadataSheetName = ".metadata";

  /*
    Create a new sheet called ".metadata", which is required for spreadsheet validation
   */
  public void addMetadataTab(Workbook workbook, String title, String version, String createdOn, String derivedFrom){
    var metadataSheet = workbook.getSheet(metadataSheetName);
    if (metadataSheet != null) {
      return;
    }

    metadataSheet = workbook.createSheet(metadataSheetName);

    var headerRow = metadataSheet.createRow(0);
    headerRow.createCell(0).setCellValue(schemaTitle);
    headerRow.createCell(1).setCellValue(pavVersion);
    headerRow.createCell(2).setCellValue(pavCreatedOn);
    headerRow.createCell(3).setCellValue(pavDerivedFrom);

    var dataRow = metadataSheet.createRow(1);
    dataRow.createCell(0).setCellValue(title);
    dataRow.createCell(1).setCellValue(version);
    dataRow.createCell(2).setCellValue(createdOn);
    dataRow.createCell(3).setCellValue(derivedFrom);
  }

  /**
   * "ESTIMATED COHORT SIZE" in original study metadata spreadsheet is stored as Text
   */
  public void patchMetadata(Workbook workbook, Path outputPath){
    var sheet = workbook.getSheetAt(0);
    var dataFormatter = new DataFormatter();

    int estimatedCohortSizeColumnIndex = findColumnIndex(sheet, "ESTIMATED COHORT SIZE");

    if (estimatedCohortSizeColumnIndex == -1) {
      System.out.println("Column with header 'ESTIMATED COHORT SIZE' not found.");
      return;
    }

    for (var row : sheet) {
      if (row.getRowNum() == 0) {
        continue;
      }

      var cell = row.getCell(estimatedCohortSizeColumnIndex);

      if (cell != null) {
        if (cell.getCellType() == CellType.NUMERIC) {
          continue; // Already a number, do nothing
        } else if (cell.getCellType() == CellType.STRING) {
          String cellValue = cell.getStringCellValue().trim();

          try {
            // Convert the string value to an integer
            int numericValue = Integer.parseInt(cellValue);
            cell.setCellValue(numericValue); // Set the cell value as a number
          } catch (NumberFormatException e) {
            System.out.println("Skipping non-numeric value: " + cellValue);
          }
        }
      }
    }

    saveWorkbookToFile(workbook, outputPath);
  }

  private void saveWorkbookToFile(Workbook workbook, Path outputPath){
    try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
      workbook.write(fos);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static int findColumnIndex(Sheet sheet, String headerName) {
    var headerRow = sheet.getRow(0); // Assuming the header is in the first row
    if (headerRow != null) {
      for (var cell : headerRow) {
        if (cell.getCellType() == CellType.STRING && cell.getStringCellValue().equalsIgnoreCase(headerName)) {
          return cell.getColumnIndex();
        }
      }
    }
    return -1; // Column not found
  }
}
