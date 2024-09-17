package bmir.radx.metadata.evaluator.thirdParty;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class SpreadsheetValidatorServiceTest {
  @Autowired
  private SpreadsheetValidatorService service;

  @Test
  public void testApiCall() throws IOException {
    String filePath = new ClassPathResource("test-data.xlsx").getFile().getAbsolutePath();

    // Call the service and perform the real API call
    var response = service.validateSpreadsheet(filePath);

    // Assert that the response is not null and contains valid data
    assertNotNull(response, "Response should not be null");
    assertTrue(response.reports().size() > 0, "Response should contain validation results");
  }
}
