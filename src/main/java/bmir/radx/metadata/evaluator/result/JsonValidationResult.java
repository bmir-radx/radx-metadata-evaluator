package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.IssueLevel;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;

public record JsonValidationResult(String studyPhs,
                                   String fileName,
                                   String pointer,
                                   IssueTypeMapping.IssueType issueType,
                                   String errorMessage,
                                   String suggestion,
                                   String uuid,
                                   IssueLevel issueLevel,
                                   String value) implements ValidationResult {
  // Constructor with default `uuid` and `issueLevel`
  public JsonValidationResult(String studyPhs,
                              String fileName,
                              String pointer,
                              IssueTypeMapping.IssueType issueType,
                              String errorMessage,
                              String suggestion,
                              String value) {
    this(studyPhs, fileName, pointer, issueType, errorMessage, suggestion, generateDefaultUUID(), IssueLevel.ERROR, value);
  }

  // Constructor with specified `issueLevel`, default `uuid`
  public JsonValidationResult(String studyPhs,
                              String fileName,
                              String pointer,
                              IssueTypeMapping.IssueType issueType,
                              String errorMessage,
                              String suggestion,
                              IssueLevel issueLevel,
                              String value) {
    this(studyPhs, fileName, pointer, issueType, errorMessage, suggestion, generateDefaultUUID(), issueLevel, value);
  }

  private static String generateDefaultUUID() {
    return java.util.UUID.randomUUID().toString();
  }
}
