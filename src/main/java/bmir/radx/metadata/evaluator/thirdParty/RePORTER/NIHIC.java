package bmir.radx.metadata.evaluator.thirdParty.RePORTER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NIHIC(
    @JsonProperty("abbreviation") String nih
) {
}
