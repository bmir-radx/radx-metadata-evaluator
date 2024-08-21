package bmir.radx.metadata.evaluator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import jakarta.annotation.Nonnull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class LlmSimilarityChecker {
  private final ObjectMapper mapper = new ObjectMapper();
  private String endpoint = "http://localhost:11434/api/chat";

  public static void main(String[] args) {
    LlmSimilarityChecker checker = new LlmSimilarityChecker();
    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.println("Enter the field name (or type 'exit' to quit):");
      String fieldName = scanner.nextLine();

      if ("exit".equalsIgnoreCase(fieldName)) {
        break;
      }

      System.out.println("Enter the user input:");
      String userInput = scanner.nextLine();

      System.out.println("Enter the permissible values (comma-separated):");
      String permissibleValuesInput = scanner.nextLine();
      List<String> permissibleValues = Arrays.asList(permissibleValuesInput.split("\\s*,\\s*"));

      String response = checker.callApiAndHandleResponse(fieldName, userInput, permissibleValues);
      System.out.println("Best match: " + response);
    }

    scanner.close();
  }

  public String callApiAndHandleResponse(String fieldName, String userInput, List<String> permissibleValues){
    try {
      var url = new URL(endpoint);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/json");
      connection.setDoOutput(true);

      var prompt = getPrompt(fieldName, userInput, permissibleValues);
      String payload = getPayLoad(prompt);

      try (OutputStream os = connection.getOutputStream()) {
        byte[] input = payload.getBytes("utf-8");
        os.write(input, 0, input.length);
      }

      int responseCode = connection.getResponseCode();
      if (responseCode == HttpURLConnection.HTTP_OK) {
        StringBuilder response;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
          response = new StringBuilder();
          String responseLine;
          while ((responseLine = br.readLine()) != null) {
            response.append(responseLine.trim());
          }
        }

        // Parse and extract the value of "message" -> "content"
        JsonNode jsonResponse = mapper.readTree(response.toString());
        String content = jsonResponse.path("message").path("content").asText();
        System.out.println("llama3.1 response: " + content);
        return content;
      } else {
        System.out.println("Error: Received HTTP code " + responseCode);
        return null;
      }

    } catch (MalformedURLException e) {
      throw new RuntimeException("Error generating URL of LLMs endpoint");
    } catch (IOException e) {
      throw new RuntimeException("Error connect to LLMs");
    }
  }

  private String getPayLoad(@Nonnull String prompt){
    var payload = ImmutableMap.of(
        "model", "llama3.1",
        "stream", false,
        "messages", ImmutableList.of(
            ImmutableMap.of(
                "role", "system",
                "content", "You are a spelling checker. Your task is to identify the closest match " +
                    "from a list of permitted values for a given field based on a specified user input. " +
                    "Return only the best matching value from the permitted list, without including the " +
                    "field name and any other descriptions in your response."),
            ImmutableMap.of(
                "role", "user",
                "content", "Field Name - Unit of Time, Permitted values - [hour, second, minute], User Input - h"
            ),
            ImmutableMap.of(
                "role", "assistant",
                "content", "hour"
            ),
            ImmutableMap.of(
                "role", "user",
                "content", prompt
            )
        )
    );

    try {
      return mapper.writeValueAsString(payload);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error while processing the ChatGPT payload string.");
    }
  }

  private String getPrompt(String fieldName, String userInput, List<String> permissibleValues) {
    var sb = new StringBuilder();
//    sb.append("Here is the example input: Field Name - Unit of Time, Permitted values - [hour, second, minute], User Input - h; Here is the example output: hour");
//    sb.append("\n");
    sb.append("Field Name - ");
    sb.append(fieldName);
    sb.append(", Permitted values - ");
    sb.append(permissibleValues);
    sb.append(", User Input - ");
    sb.append(userInput);
    sb.append("; Output: ");
    return sb.toString();
  }
}
