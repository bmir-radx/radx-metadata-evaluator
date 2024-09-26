package bmir.radx.metadata.evaluator.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest;
import java.util.List;

public class DescriptionExplorer {
  private static final HttpClient httpClient = HttpClient.newHttpClient();
  private static final ObjectMapper objectMapper = new ObjectMapper();

  public void processMetadata(List<StudyMetadataRow> metadataRows, String outputFilePath) {
    try (Workbook workbook = new XSSFWorkbook()) {
      Sheet sheet = workbook.createSheet("Main");
      createHeaderRow(sheet);

      int rowIndex = 1;
      for (StudyMetadataRow row : metadataRows) {
        String clinicalTrialsGovUrl = row.clinicalTrialsGovUrl();
        String nctId = extractNctId(clinicalTrialsGovUrl);

        if (nctId != null) {
          // Send GET request and retrieve the JSON response
          String jsonResponse = sendGetRequest(nctId);
          if (jsonResponse != null) {
            JsonNode protocolSectionNode = objectMapper.readTree(jsonResponse).get("protocolSection");
            JsonNode identificationModule = protocolSectionNode.get("identificationModule");
            String briefTitle = identificationModule.has("briefTitle") ? identificationModule.get("briefTitle").asText(): "";
            String officialTitle = identificationModule.has("officialTitle") ? identificationModule.get("briefTitle").asText(): "";
//            System.out.println("Row Number: " + row.rowNumber());
//            System.out.println("RADx Title: " + row.studyTitle());
//            System.out.println("Brief title: " + briefTitle);
//            System.out.println("Official title: " + officialTitle);
//            System.out.println("----------------------------");

            // Parse the JSON response
            JsonNode descriptionModuleNode = protocolSectionNode.get("descriptionModule");
            if (descriptionModuleNode != null) {
              String briefSummary = descriptionModuleNode.has("briefSummary") ? descriptionModuleNode.get("briefSummary").asText() : "";
              String detailedDescription = descriptionModuleNode.has("detailedDescription") ? descriptionModuleNode.get("detailedDescription").asText() : "";
              System.out.println(briefSummary);
              System.out.println(detailedDescription);
              System.out.println("---------------------------------");

              // Store the values in the spreadsheet
              Row excelRow = sheet.createRow(rowIndex++);
              storeMetadataInRow(excelRow, row, briefSummary, detailedDescription);
            }
          }
        }
      }

      // Write the workbook to the file
      try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
        workbook.write(fos);
      }

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private String extractNctId(String clinicalTrialsGovUrl) {
    if (clinicalTrialsGovUrl != null && clinicalTrialsGovUrl.contains("/NCT")) {
      return clinicalTrialsGovUrl.substring(clinicalTrialsGovUrl.lastIndexOf("/") + 1);
    }
    return null;
  }

  private String sendGetRequest(String nctId) throws IOException, InterruptedException {
    String url = "https://clinicaltrials.gov/api/v2/studies/" + nctId;

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("accept", "application/json")
        .GET()
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() == 200) {
      return response.body();
    }
    return null;
  }

  private void createHeaderRow(Sheet sheet) {
    Row headerRow = sheet.createRow(0);
    headerRow.createCell(0).setCellValue("Row Number");
    headerRow.createCell(1).setCellValue("Study PHS");
    headerRow.createCell(2).setCellValue("Study Title");
    headerRow.createCell(3).setCellValue("RADx Description");
    headerRow.createCell(4).setCellValue("Brief Summary");
    headerRow.createCell(5).setCellValue("Detailed Description");
    headerRow.createCell(6).setCellValue("Study Design");
    headerRow.createCell(7).setCellValue("Study Domain");
    headerRow.createCell(8).setCellValue("Keywords");
    headerRow.createCell(9).setCellValue("Data Collection Method");
    headerRow.createCell(10).setCellValue("Estimated Cohort Size");
    headerRow.createCell(11).setCellValue("Population Focus");
    headerRow.createCell(12).setCellValue("Species");
    headerRow.createCell(13).setCellValue("Multi-Center Sites");
    headerRow.createCell(14).setCellValue("clinicaltrials.gov URL");
  }

  private void storeMetadataInRow(Row excelRow, StudyMetadataRow metadataRow, String briefSummary, String detailedDescription) {
    excelRow.createCell(0).setCellValue(metadataRow.rowNumber());
    excelRow.createCell(1).setCellValue(metadataRow.studyPHS());
    excelRow.createCell(2).setCellValue(metadataRow.studyTitle());
    excelRow.createCell(3).setCellValue(metadataRow.description());
    excelRow.createCell(4).setCellValue(briefSummary);
    excelRow.createCell(5).setCellValue(detailedDescription);
    excelRow.createCell(6).setCellValue(metadataRow.studyDesign());
    excelRow.createCell(7).setCellValue(metadataRow.studyDomain());
    excelRow.createCell(8).setCellValue(metadataRow.keywords());
    excelRow.createCell(9).setCellValue(metadataRow.dataCollectionMethod());
    excelRow.createCell(10).setCellValue(metadataRow.estimatedCohortSize());
    excelRow.createCell(11).setCellValue(metadataRow.studyPopulationFocus());
    excelRow.createCell(12).setCellValue(metadataRow.species());
    excelRow.createCell(13).setCellValue(metadataRow.multiCenterSites());
    excelRow.createCell(14).setCellValue(metadataRow.clinicalTrialsGovUrl());
  }
}
