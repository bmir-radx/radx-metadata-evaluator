package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.util.IssueTypeMapping;

public record SpreadsheetValidationResult(IssueTypeMapping.IssueType issueType,
                                          String column,
                                          int row,
                                          String phsNumber,
                                          String repairSuggestion,
                                          Object value) implements ValidationResult {
}
