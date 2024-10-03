package bmir.radx.metadata.evaluator.thirdParty.clinicalTrials;

import bmir.radx.metadata.evaluator.thirdParty.RestServiceHandler;
import bmir.radx.metadata.evaluator.thirdParty.SpreadsheetValidatorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Mono;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Service
public class ClinicalTrialsService {
  @Value("${clinicalTrials.service.url}")
  private String urlString;

  private final RestServiceHandler restServiceHandler;

  public ClinicalTrialsService(RestServiceHandler restServiceHandler) {
    this.restServiceHandler = restServiceHandler;
  }

  public ClinicalTrialsResponse sendGetRequest(String nctId) {
    Map<String, String> headers = new HashMap<>();
    headers.put("accept", "application/json");
    var url = urlString + nctId;

    return restServiceHandler
        .sendRequest(url, headers, null, HttpMethod.GET)
        .retrieve()
        .onStatus(
            status -> !status.is2xxSuccessful(),  // Check if status is not 2xx (like 200)
            clientResponse -> Mono.empty()
        )
        .bodyToMono(ClinicalTrialsResponse.class)
        .blockOptional()
        .orElse(null);
  }
}
