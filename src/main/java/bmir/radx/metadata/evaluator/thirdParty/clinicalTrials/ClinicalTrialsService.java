package bmir.radx.metadata.evaluator.thirdParty.clinicalTrials;

import edu.stanford.bmir.radx.metadata.validator.lib.thirdPartyValidators.RestServiceHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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
            clientResponse -> Mono.error(new RuntimeException("ClinicalTrials GET request failed with status: " + clientResponse.statusCode()))
        )
        .bodyToMono(ClinicalTrialsResponse.class)
        .blockOptional()
        .orElse(null);
  }
}
