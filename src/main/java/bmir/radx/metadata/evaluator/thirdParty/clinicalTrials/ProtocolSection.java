package bmir.radx.metadata.evaluator.thirdParty.clinicalTrials;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProtocolSection(
    @JsonProperty("identificationModule") IdentificationModule identificationModule,
    @JsonProperty("descriptionModule") DescriptionModule descriptionModule
) {
}
