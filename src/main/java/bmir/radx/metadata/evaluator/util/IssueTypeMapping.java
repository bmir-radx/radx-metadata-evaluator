package bmir.radx.metadata.evaluator.util;

import edu.stanford.bmir.radx.metadata.validator.lib.ValidationName;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class IssueTypeMapping {
    public enum IssueType {
        MISSING_REQUIRED,
        INVALID_JSON,
        INVALID_CEDAR_MODEL,
        SANITATION_ISSUE,
        INVALID_JSON_SCHEMA,
        INVALID_INSTANCE_ARTIFACT_SCHEMA,
        CONSTRAINT_VIOLATION,
        CARDINALITY_ISSUE,
        LITERAL_FIELD_ISSUE,
        UNKNOWN_ISSUE,
        INACCURATE_RECORD,
        INCONSISTENT_RECORD,
        DUPLICATE_RECORD,
        INVALID_SCHEMA_ID,
        NON_STANFORD_TERM,
        INVALID_VALUE_ENCODING,
        INVALID_URL
    }

    private static final Map<ValidationName, IssueType> validationToIssueTypeMap = new EnumMap<>(ValidationName.class);
    private static final Map<String, IssueType> stringToIssueTypeMap = new HashMap<>();

    static {
        // Mapping ValidationName to IssueType
        validationToIssueTypeMap.put(ValidationName.JSON_VALIDATION, IssueType.INVALID_JSON);
        validationToIssueTypeMap.put(ValidationName.CEDAR_MODEL_VALIDATION, IssueType.INVALID_CEDAR_MODEL);
        validationToIssueTypeMap.put(ValidationName.SANITATION_CHECK, IssueType.SANITATION_ISSUE);
        validationToIssueTypeMap.put(ValidationName.SCHEMA_VALIDATION, IssueType.INVALID_JSON_SCHEMA);
        validationToIssueTypeMap.put(ValidationName.REQUIREMENT_VALIDATION, IssueType.MISSING_REQUIRED);
        validationToIssueTypeMap.put(ValidationName.ARTIFACT_SCHEMA_VALIDATION, IssueType.INVALID_INSTANCE_ARTIFACT_SCHEMA);
        validationToIssueTypeMap.put(ValidationName.DATA_TYPE_VALIDATION, IssueType.CONSTRAINT_VIOLATION);
        validationToIssueTypeMap.put(ValidationName.CARDINALITY_VALIDATION, IssueType.CARDINALITY_ISSUE);
        validationToIssueTypeMap.put(ValidationName.LITERAL_FIELD_VALIDATION, IssueType.LITERAL_FIELD_ISSUE);
        validationToIssueTypeMap.put(ValidationName.UNKNOWN, IssueType.UNKNOWN_ISSUE);

        // Mapping fixed strings to IssueType
        stringToIssueTypeMap.put("numberOutOfRange", IssueType.CONSTRAINT_VIOLATION);
        stringToIssueTypeMap.put("invalidSchemaId", IssueType.INVALID_SCHEMA_ID);
        stringToIssueTypeMap.put("notNumberType", IssueType.CONSTRAINT_VIOLATION);
        stringToIssueTypeMap.put("notStanfordTerm", IssueType.NON_STANFORD_TERM);
        stringToIssueTypeMap.put("missingRequired", IssueType.MISSING_REQUIRED);
        stringToIssueTypeMap.put("invalidValueFormat", IssueType.CONSTRAINT_VIOLATION);
        stringToIssueTypeMap.put("notStringType", IssueType.CONSTRAINT_VIOLATION);
        stringToIssueTypeMap.put("invalidValueEncoding", IssueType.INVALID_VALUE_ENCODING);
        stringToIssueTypeMap.put("invalidUrl", IssueType.INVALID_URL);
    }

    public static IssueType getIssueType(ValidationName validationName) {
        return validationToIssueTypeMap.getOrDefault(validationName, IssueType.UNKNOWN_ISSUE);
    }

    public static IssueType getIssueType(String issueTypeString) {
        return stringToIssueTypeMap.getOrDefault(issueTypeString, IssueType.UNKNOWN_ISSUE);
    }
}
