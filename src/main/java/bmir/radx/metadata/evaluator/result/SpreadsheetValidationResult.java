package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.IssueLevel;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;

public record SpreadsheetValidationResult(IssueTypeMapping.IssueType issueType,
                                          String column,
                                          int row,
                                          String studyPhs,
                                          String repairSuggestion,
                                          Object value,
                                          String uuid,
                                          IssueLevel issueLevel) implements ValidationResult {
  // Constructor with default `uuid` and `issueLevel`
  public SpreadsheetValidationResult(IssueTypeMapping.IssueType issueType,
                                     String column,
                                     int row,
                                     String phsNumber,
                                     String repairSuggestion,
                                     Object value) {
    this(issueType, column, row, phsNumber, repairSuggestion, value, generateDefaultUUID(), IssueLevel.ERROR);
  }

  // Constructor with specified `issueLevel`, default `uuid`
  public SpreadsheetValidationResult(IssueTypeMapping.IssueType issueType,
                                     String column,
                                     int row,
                                     String phsNumber,
                                     String repairSuggestion,
                                     Object value,
                                     IssueLevel issueLevel) {
    this(issueType, column, row, phsNumber, repairSuggestion, value, generateDefaultUUID(), issueLevel);
  }

  private static String generateDefaultUUID() {
    return java.util.UUID.randomUUID().toString();
  }
}
