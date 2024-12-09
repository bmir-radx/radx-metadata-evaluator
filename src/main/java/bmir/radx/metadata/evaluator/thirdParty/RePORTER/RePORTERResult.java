package bmir.radx.metadata.evaluator.thirdParty.RePORTER;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public record RePORTERResult(
    @JsonProperty("agency_ic_admin") NIHIC nihic,
    @JsonProperty("opportunity_number") String foaNumber,
    @JsonProperty("contact_pi_name") String contactPIName
) {
}
