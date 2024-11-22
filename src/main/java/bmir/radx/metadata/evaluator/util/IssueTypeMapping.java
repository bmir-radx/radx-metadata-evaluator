package bmir.radx.metadata.evaluator.util;

import edu.stanford.bmir.radx.metadata.validator.lib.ValidationName;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class IssueTypeMapping {
    public enum IssueType {
        MISSING_REQUIRED("Missing Required Field"),
        INVALID_JSON("Invalid JSON Format"),
        INVALID_CEDAR_MODEL("Invalid CEDAR Model"),
        SANITATION_ISSUE("Sanitation Issue"),
        INVALID_JSON_SCHEMA("Invalid JSON Schema"),
        INVALID_INSTANCE_ARTIFACT_SCHEMA("Invalid Instance Artifact Schema"),
        CONSTRAINT_VIOLATION("Constraint Violation"),
        CARDINALITY_ISSUE("Cardinality Issue"),
        LITERAL_FIELD_ISSUE("Literal Field Issue"),
        UNKNOWN_ISSUE("Unknown Issue"),
        INACCURATE_FIELD("Inaccurate Field"),
        INCONSISTENT_FIELD("Inconsistent Field"),
        DUPLICATE_RECORD("Duplicate Record"),
        INVALID_SCHEMA_ID("Invalid Schema ID"),
        NON_STANFORD_TERM("Non-Stanford Term"),
        INVALID_VALUE_ENCODING("Invalid Value Encoding"),
        INVALID_URL("Invalid URL");

        private final String name;

        // Constructor to initialize the name
        IssueType(String name) {
            this.name = name;
        }

        // Getter for the name
        public String getName() {
            return name;
        }
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
