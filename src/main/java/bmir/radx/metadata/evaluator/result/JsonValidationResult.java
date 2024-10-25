package bmir.radx.metadata.evaluator.result;

import edu.stanford.bmir.radx.metadata.validator.lib.ValidationName;

public record JsonValidationResult(String fileName,
                                   String pointer,
                                   ValidationName validationName,
                                   String errorMessage,
                                   String suggestion) implements ValidationResult {
}
