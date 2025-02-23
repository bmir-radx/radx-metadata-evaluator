package bmir.radx.metadata.evaluator.util;

import edu.stanford.bmir.radx.metadata.validator.lib.ValidationName;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class IssueTypeMapping {
    public enum IssueType {
//        MISSING_REQUIRED("Missing Required Field"),
//        INVALID_JSON("Invalid JSON Format"),
//        INVALID_CEDAR_MODEL("Invalid CEDAR Model"),
//        SANITATION_ISSUE("Sanitation Issue"),
//        INVALID_JSON_SCHEMA("Invalid JSON Schema"),
//        INVALID_INSTANCE_ARTIFACT_SCHEMA("Invalid Instance Artifact Schema"),
//        CONSTRAINT_VIOLATION("Constraint Violation"),
        VALIDITY("Validity"),
//        LITERAL_FIELD_ISSUE("Literal Field Issue"),
        UNKNOWN_ISSUE("Unknown Issue"),
        ACCURACY("Accuracy"),
        CONSISTENCY("Consistency"),
        OVERALL_COMPLETENESS("Overall Completeness"),
        REQUIRED_COMPLETENESS("Required Fields Completeness"),
        RECOMMENDED_COMPLETENESS("Recommended Fields Completeness"),
        OPTIONAL_COMPLETENESS("Optional Fields Completeness"),
        UNIQUENESS("Uniqueness"),
//        INVALID_SCHEMA_ID("Invalid Schema ID"),
        CONTROLLED_VOCABULARY_CONSISTENCY("Controlled Vocabulary Consistency"),
//        INVALID_VALUE_ENCODING("Invalid Value Encoding"),
        ACCESSIBILITY("Accessibility"),
        STRUCTURAL_QUALITY("Structural Quality"),
        LINGUISTIC_QUALITY("Linguistic Quality");

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
//        validationToIssueTypeMap.put(ValidationName.JSON_VALIDATION, IssueType.INVALID_JSON);
//        validationToIssueTypeMap.put(ValidationName.CEDAR_MODEL_VALIDATION, IssueType.INVALID_CEDAR_MODEL);
//        validationToIssueTypeMap.put(ValidationName.SANITATION_CHECK, IssueType.SANITATION_ISSUE);
//        validationToIssueTypeMap.put(ValidationName.SCHEMA_VALIDATION, IssueType.INVALID_JSON_SCHEMA);
//        validationToIssueTypeMap.put(ValidationName.REQUIREMENT_VALIDATION, IssueType.MISSING_REQUIRED);
//        validationToIssueTypeMap.put(ValidationName.ARTIFACT_SCHEMA_VALIDATION, IssueType.INVALID_INSTANCE_ARTIFACT_SCHEMA);
//        validationToIssueTypeMap.put(ValidationName.DATA_TYPE_VALIDATION, IssueType.CONSTRAINT_VIOLATION);
//        validationToIssueTypeMap.put(ValidationName.LITERAL_FIELD_VALIDATION, IssueType.LITERAL_FIELD_ISSUE);

        validationToIssueTypeMap.put(ValidationName.JSON_VALIDATION, IssueType.VALIDITY);
        validationToIssueTypeMap.put(ValidationName.CEDAR_MODEL_VALIDATION, IssueType.VALIDITY);
        validationToIssueTypeMap.put(ValidationName.SANITATION_CHECK, IssueType.VALIDITY);
        validationToIssueTypeMap.put(ValidationName.SCHEMA_VALIDATION, IssueType.VALIDITY);
        validationToIssueTypeMap.put(ValidationName.REQUIREMENT_VALIDATION, IssueType.VALIDITY);
        validationToIssueTypeMap.put(ValidationName.ARTIFACT_SCHEMA_VALIDATION, IssueType.VALIDITY);
        validationToIssueTypeMap.put(ValidationName.DATA_TYPE_VALIDATION, IssueType.VALIDITY);
        validationToIssueTypeMap.put(ValidationName.LITERAL_FIELD_VALIDATION, IssueType.VALIDITY);

        validationToIssueTypeMap.put(ValidationName.CARDINALITY_VALIDATION, IssueType.VALIDITY);
        validationToIssueTypeMap.put(ValidationName.CONTROLLED_TERM_VALIDATION, IssueType.CONTROLLED_VOCABULARY_CONSISTENCY);
        validationToIssueTypeMap.put(ValidationName.UNKNOWN, IssueType.UNKNOWN_ISSUE);

        // Mapping fixed strings to IssueType
//        stringToIssueTypeMap.put("numberOutOfRange", IssueType.CONSTRAINT_VIOLATION);
//        stringToIssueTypeMap.put("invalidSchemaId", IssueType.INVALID_SCHEMA_ID);
//        stringToIssueTypeMap.put("notNumberType", IssueType.CONSTRAINT_VIOLATION);
//        stringToIssueTypeMap.put("missingRequired", IssueType.MISSING_REQUIRED);
//        stringToIssueTypeMap.put("invalidValueFormat", IssueType.CONSTRAINT_VIOLATION);
//        stringToIssueTypeMap.put("notStringType", IssueType.CONSTRAINT_VIOLATION);
//        stringToIssueTypeMap.put("invalidValueEncoding", IssueType.INVALID_VALUE_ENCODING);

        stringToIssueTypeMap.put("numberOutOfRange", IssueType.VALIDITY);
        stringToIssueTypeMap.put("invalidSchemaId", IssueType.VALIDITY);
        stringToIssueTypeMap.put("notNumberType", IssueType.VALIDITY);
        stringToIssueTypeMap.put("missingRequired", IssueType.VALIDITY);
        stringToIssueTypeMap.put("invalidValueFormat", IssueType.VALIDITY);
        stringToIssueTypeMap.put("notStringType", IssueType.VALIDITY);
        stringToIssueTypeMap.put("invalidValueEncoding", IssueType.VALIDITY);

        stringToIssueTypeMap.put("notStanfordTerm", IssueType.CONTROLLED_VOCABULARY_CONSISTENCY);
        stringToIssueTypeMap.put("invalidUrl", IssueType.ACCESSIBILITY);
    }

    public static IssueType getIssueType(ValidationName validationName) {
        return validationToIssueTypeMap.getOrDefault(validationName, IssueType.UNKNOWN_ISSUE);
    }

    public static IssueType getIssueType(String issueTypeString) {
        return stringToIssueTypeMap.getOrDefault(issueTypeString, IssueType.UNKNOWN_ISSUE);
    }

    public static IssueType getIssueType(FieldCategory fieldCategory){
      if (fieldCategory.equals(FieldCategory.OVERALL)){
        return IssueType.OVERALL_COMPLETENESS;
      } else if (fieldCategory.equals(FieldCategory.REQUIRED)) {
        return IssueType.REQUIRED_COMPLETENESS;
      } else if (fieldCategory.equals(FieldCategory.RECOMMENDED)) {
        return IssueType.RECOMMENDED_COMPLETENESS;
      } else {
        return IssueType.OPTIONAL_COMPLETENESS;
      }
    }
}
