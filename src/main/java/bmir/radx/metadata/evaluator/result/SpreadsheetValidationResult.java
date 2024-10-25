package bmir.radx.metadata.evaluator.result;

public record SpreadsheetValidationResult(String errorType,
                                          String column,
                                          int row,
                                          String phsNumber,
                                          String repairSuggestion,
                                          Object value) implements ValidationResult {
}
