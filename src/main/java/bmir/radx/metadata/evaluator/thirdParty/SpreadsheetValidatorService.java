package bmir.radx.metadata.evaluator.thirdParty;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class SpreadsheetValidatorService {
  @Value("${spreadsheet.validator.service.url}")
  private String urlString;

  private final RestServiceHandler restServiceHandler;

  public SpreadsheetValidatorService(RestServiceHandler restServiceHandler) {
    this.restServiceHandler = restServiceHandler;
  }

  public SpreadsheetValidatorResponse validateSpreadsheet(String filePath) {
    Map<String, String> headers = new HashMap<>();
    headers.put("accept", "application/json");
    headers.put("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary");

    MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
    File file = new File(filePath);

    body.add("input_file", new FileSystemResource(file));

    return restServiceHandler
        .sendRequest(urlString, headers, body, HttpMethod.POST)
        .retrieve()
        .bodyToMono(SpreadsheetValidatorResponse.class)
        .block();
  }
}
