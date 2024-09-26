package bmir.radx.metadata.evaluator.thirdParty;

import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
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
public record SpreadsheetValidatorResponse(@JsonProperty("reporting") List<SpreadsheetValidationResult> reports) {
}
