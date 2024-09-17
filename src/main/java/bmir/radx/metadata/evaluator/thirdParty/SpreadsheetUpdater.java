package bmir.radx.metadata.evaluator.thirdParty;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

  public void saveWorkbookToFile(Workbook workbook, Path outputPath){
    try (FileOutputStream fos = new FileOutputStream(outputPath.toFile())) {
      workbook.write(fos);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
