package bmir.radx.metadata.evaluator.thirdParty.clinicalTrials;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record IdentificationModule(
    @JsonProperty("briefTitle") String briefTitle,
    @JsonProperty("officialTitle") String officialTitle
) {
}
