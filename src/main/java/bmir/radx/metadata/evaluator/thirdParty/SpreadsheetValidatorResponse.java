package bmir.radx.metadata.evaluator.thirdParty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


/**
 * {
 * schema	{...}
 * data	[...]
 * reporting	[...]
 * }
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SpreadsheetValidatorResponse(@JsonProperty("reporting") List<SpreadsheetValidatorReport> reports) {
}
