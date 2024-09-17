package bmir.radx.metadata.evaluator.thirdParty;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class RestServiceHandler {
  private final WebClient webClient;

  public RestServiceHandler(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.build();
  }

  public WebClient.RequestBodySpec sendRequest(String url, Map<String, String> headers, Object body, HttpMethod httpMethod) {
    WebClient.RequestBodySpec requestSpec = webClient.method(httpMethod).uri(url);

    for (Map.Entry<String, String> header : headers.entrySet()) {
      requestSpec.header(header.getKey(), header.getValue());
    }

    if (httpMethod == HttpMethod.POST || httpMethod == HttpMethod.PUT) {
      requestSpec.bodyValue(body);
    }

    return requestSpec;
  }
}
