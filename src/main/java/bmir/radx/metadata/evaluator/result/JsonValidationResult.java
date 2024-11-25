package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.util.IssueTypeMapping;

public record JsonValidationResult(String studyPhs,
                                   String fileName,
                                   String pointer,
                                   IssueTypeMapping.IssueType issueType,
                                   String errorMessage,
                                   String suggestion) implements ValidationResult {
}
