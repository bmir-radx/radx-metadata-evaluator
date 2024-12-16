package bmir.radx.metadata.evaluator.thirdParty.rePORTER;

import edu.stanford.bmir.radx.metadata.validator.lib.thirdPartyValidators.RestServiceHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class RePORTERService {
  @Value("${reporter.service.url}")
  private String url;

  private final RestServiceHandler restServiceHandler;


  public RePORTERService(RestServiceHandler restServiceHandler) {
    this.restServiceHandler = restServiceHandler;
  }

  public RePORTERResponse sendPostRequest(String nihGrantNumber){
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", MediaType.APPLICATION_JSON_VALUE);

    String requestBody = String.format("{ \"criteria\": { \"project_nums\": [\"%s\"] } }", nihGrantNumber);

    return restServiceHandler
        .sendRequest(url, headers, requestBody, HttpMethod.POST)
        .retrieve()
        .onStatus(
            status -> !status.is2xxSuccessful(),
            response -> Mono.error(new RuntimeException("RePORTER POST request failed with status: " + response.statusCode()))
        )
        .bodyToFlux(RePORTERResponse.class)
        .next()
        .block();
  }
}
