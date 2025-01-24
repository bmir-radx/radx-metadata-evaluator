package bmir.radx.metadata.evaluator.thirdParty.rePORTER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PI(
    @JsonProperty("first_name") String firstName,
    @JsonProperty("middle_name") String middleName,
    @JsonProperty("last_name") String lastName,
    @JsonProperty("full_name") String fullName
) {
}
