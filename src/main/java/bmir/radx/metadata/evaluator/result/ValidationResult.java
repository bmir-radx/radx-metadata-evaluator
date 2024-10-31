package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.util.IssueTypeMapping;

public interface ValidationResult extends Result {
    IssueTypeMapping.IssueType issueType();
}
