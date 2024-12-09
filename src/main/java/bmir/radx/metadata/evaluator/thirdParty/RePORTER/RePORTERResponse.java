package bmir.radx.metadata.evaluator.thirdParty.RePORTER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RePORTERResponse(
    @JsonProperty("results") List<RePORTERResult> results
) {
}
