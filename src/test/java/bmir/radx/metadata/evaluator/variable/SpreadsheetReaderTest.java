package bmir.radx.metadata.evaluator.variable;

import bmir.radx.metadata.evaluator.SpreadsheetReader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpreadsheetReaderTest {
  private SpreadsheetReader spreadsheetReader = new SpreadsheetReader();
  @Test
  public void testReadVariablesMetadata() throws IOException {
    // Path to the test spreadsheet file
    String filePath = "src/test/resources/integrated_variable_metadata.xlsx";

    // Read the spreadsheet file
    try (FileInputStream fileInputStream = new FileInputStream(new File(filePath))) {
      List<VariableMetadataRow> metadataList = spreadsheetReader.readVariablesMetadata(fileInputStream);

      // Perform your assertions here
      assertEquals(8836, metadataList.size()); // Example assertion, adjust based on your test data

      var metadata = metadataList.get(0);
      assertEquals("1", metadata.dataVariable());
      assertEquals(false, metadata.isTier1CDE());
      assertEquals(1, metadata.fileCount());
      assertEquals(1, metadata.studyCount());
      assertEquals(List.of("phs002747"), metadata.dbGaPIDs());
      assertEquals(List.of("phs002747 - rad_017_812-01_07012021to09302021_DATA_origcopy_v1.csv"), metadata.filesPerStudy());
      assertEquals(List.of("RADx-rad"), metadata.radxProgram());
      assertEquals(null, metadata.label());
      assertEquals(null, metadata.concept());
      assertEquals(null, metadata.responses());
      assertEquals(null, metadata.radxGlobalPrompt());
    }
  }

  @Test
  public void testReadGlobalCodeBook() throws IOException {
    // Path to the test spreadsheet file
    String url = "https://docs.google.com/spreadsheets/d/1famf1rpRpLz3Q-rLJ5t-pEbetPWasxsK/export?format=xlsx&id=1famf1rpRpLz3Q-rLJ5t-pEbetPWasxsK&gid=200816038";

    // Read the spreadsheet file
    try (var inputStream = new URL(url).openStream()) {
      var globalCodeBookRows = spreadsheetReader.readGlobalCodeBook(inputStream);

      // Perform your assertions here
      assertEquals(133, globalCodeBookRows.size()); // Example assertion, adjust based on your test data

      var cde = globalCodeBookRows.get(0);
      assertEquals("Identity", cde.concept());
      assertEquals("User ID", cde.radxGlobalPrompt());
      assertEquals("nih_record_id", cde.variable());
      assertEquals("text ", cde.responses());
    }
  }
}
