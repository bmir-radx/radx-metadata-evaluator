package bmir.radx.metadata.evaluator.thirdParty;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import edu.stanford.bmir.radx.metadata.validator.lib.thirdPartyValidators.RestServiceHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OllamaService {
  @Value("${ollama.service.url}")
  private String urlString;
  private final RestServiceHandler restServiceHandler;

  public OllamaService(RestServiceHandler restServiceHandler) {
    this.restServiceHandler = restServiceHandler;
  }

  public OllamaResponse sendRequest(String t1, String t2, String d1, String d2){
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");
    var body = getPayload(t1, t2, d1, d2);

    return restServiceHandler
        .sendRequest(urlString, headers, body, HttpMethod.POST)
        .retrieve()
        .bodyToMono(OllamaResponse.class)
        .block();
  }

  private String getPrompt(String t1, String t2, String d1, String d2){
    var sb = new StringBuilder();
    sb.append("Title1: ");
    sb.append(t1).append("\n");
    sb.append("Tittle2: ");
    sb.append(t2).append("\n");
    sb.append("Description1: ");
    sb.append(d1).append("\n");
    sb.append("Description2: ");
    sb.append(d2);
    return sb.toString();
  }

  private JsonNode getPayload(String t1, String t2, String d1, String d2){
    var mapper = new ObjectMapper();
    var prompt = getPrompt(t1, t2, d1, d2);
    var payLoad = ImmutableMap.of(
        "model", "llama3.2",
        "messages", ImmutableList.of(
              ImmutableMap.of(
                  "role", "system",
                  "content", "Compare the following two titles and descriptions, each from a different source, to determine if they describe the same study.\n" +
                      "First, compare the titles: if they are semantically similar or share the same keywords, conclude they are the same study.\n" +
                      "If the titles differ, compare the descriptions. If the study objective, design, methods, or outcomes are similar, conclude they are the same study.\n" +
                      "Respond with a simple 'Yes' or 'No'.\""
              ),
              ImmutableMap.of(
                  "role", "user",
                  "content", "Title1 : Personalized Analytics and Wearable Biosensor Platform for Early Detection of COVID-19 Decompensation (DECODE). \n" +
                      "Title2 : Personalized Analytics and Wearable Biosensor Platform for Early Detection of COVID-19 Decompensation. \n" +
                      "Description1: This study is a randomized control trial aimed at assessing the effectiveness of Drug A in reducing blood pressure in patients diagnosed with hypertension." +
                      "A total of 500 participants will be monitored over a period of 12 weeks, with blood pressure measurements taken at regular intervals.\n" +
                      "Description2 : This clinical trial evaluates the effect of Drug A on lowering blood pressure in hypertensive patients. " +
                      "The study involves 500 participants who will be observed for 12 weeks, with regular assessments of their blood pressure to measure the drug's efficacy."
              ),
            ImmutableMap.of(
                "role", "assistant",
                "content", "Yes"
            ),
            ImmutableMap.of(
                "role", "user",
                "content", prompt
            )
        ),
        "stream", false,
        "options", ImmutableMap.of(
          "seed", 101,
            "temperature", 0
        )
    );
    try {
      String jsonString = mapper.writeValueAsString(payLoad);
      return mapper.readTree(jsonString);
//      return mapper.writeValueAsString(payLoad);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error reading payload as JsonNode");
    }
  }
}
