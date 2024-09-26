package bmir.radx.metadata.evaluator.result;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpreadsheetValidationResult(@JsonProperty("errorType") String errorType,
                                          @JsonProperty("column") String column,
                                          @JsonProperty("row") int row,
                                          @JsonProperty("repairSuggestion") String repairSuggestion,
                                          @JsonProperty("value") Object value) implements Result {
}
