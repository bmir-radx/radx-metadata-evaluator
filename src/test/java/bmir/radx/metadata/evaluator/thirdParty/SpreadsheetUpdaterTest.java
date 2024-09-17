package bmir.radx.metadata.evaluator.thirdParty;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SpreadsheetUpdaterTest {
  private SpreadsheetUpdater spreadsheetUpdater;

  @BeforeEach
  void setUp() {
    spreadsheetUpdater = new SpreadsheetUpdater();
  }

  @Test
  void testAddMetadataTab() throws IOException {
    var workbook = new XSSFWorkbook();

    spreadsheetUpdater.addMetadataTab(workbook, "My Title", "1.0", "2024-09-08", "Derived From Example");

    // Check if the new ".metadata" sheet exists
    var metadataSheet = workbook.getSheet(".metadata");
    assertNotNull(metadataSheet, "The .metadata sheet should exist");

    // Check the header row values
    var headerRow = metadataSheet.getRow(0);
    assertNotNull(headerRow, "Header row should not be null");
    assertEquals("schema:title", headerRow.getCell(0).getStringCellValue());
    assertEquals("pav:version", headerRow.getCell(1).getStringCellValue());
    assertEquals("pav:createdOn", headerRow.getCell(2).getStringCellValue());
    assertEquals("pav:derivedFrom", headerRow.getCell(3).getStringCellValue());

    // Check the data row values
    var dataRow = metadataSheet.getRow(1);
    assertNotNull(dataRow, "Data row should not be null");
    assertEquals("My Title", dataRow.getCell(0).getStringCellValue());
    assertEquals("1.0", dataRow.getCell(1).getStringCellValue());
    assertEquals("2024-09-08", dataRow.getCell(2).getStringCellValue());
    assertEquals("Derived From Example", dataRow.getCell(3).getStringCellValue());
  }
}
