package bmir.radx.metadata.evaluator.thirdParty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaResponse(
    @JsonProperty("model") String model,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("message") Message message,
    @JsonProperty("done") boolean done,
    @JsonProperty("total_duration") long totalDuration,
    @JsonProperty("load_duration") long loadDuration,
    @JsonProperty("prompt_eval_count") int promptEvalCount,
    @JsonProperty("prompt_eval_duration") long promptEvalDuration,
    @JsonProperty("eval_count") int evalCount,
    @JsonProperty("eval_duration") long evalDuration
) {
}
