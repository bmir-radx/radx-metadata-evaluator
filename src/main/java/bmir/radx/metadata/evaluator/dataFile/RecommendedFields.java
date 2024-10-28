package bmir.radx.metadata.evaluator.dataFile;

import edu.stanford.bmir.radx.metadata.validator.lib.FieldPath;

import java.util.HashSet;
import java.util.Set;

public enum RecommendedFields {
    // Data File Identity paths
    DATA_FILE_IDENTITY_IDENTIFIER(new FieldPath("Data File Identity", "Identifier")),
    DATA_FILE_IDENTITY_FILE_NAME(new FieldPath("Data File Identity", "File Name")),
    DATA_FILE_IDENTITY_VERSION(new FieldPath("Data File Identity", "Version")),
    DATA_FILE_IDENTITY_SHA256_DIGEST(new FieldPath("Data File Identity", "SHA256 digest")),
    // Data File Subjects paths
    DATA_FILE_SUBJECTS_SUBJECT_IDENTIFIER(new FieldPath("Data File Subjects", "Subject Identifier")),
    // Data File Descriptions paths
    DATA_FILE_DESCRIPTIONS_DESCRIPTION(new FieldPath("Data File Descriptions", "Description")),
    DATA_FILE_DESCRIPTIONS_TYPE_OF_CONTENT(new FieldPath("Data File Descriptions", "Type Of Content")),
    // Data File Creators paths
    DATA_FILE_CREATORS_CREATOR_TYPE(new FieldPath("Data File Creators", "Creator Type")),
    DATA_FILE_CREATORS_CREATOR_NAME(new FieldPath("Data File Creators", "Creator Name")),
    DATA_FILE_CREATORS_CREATOR_GIVEN_NAME(new FieldPath("Data File Creators", "Creator Given Name")),
    DATA_FILE_CREATORS_CREATOR_FAMILY_NAME(new FieldPath("Data File Creators", "Creator Family Name")),
    DATA_FILE_CREATORS_CREATOR_IDENTIFIER(new FieldPath("Data File Creators", "Creator Identifier")),
    DATA_FILE_CREATORS_CREATOR_EMAIL(new FieldPath("Data File Creators", "Creator Email")),
    DATA_FILE_CREATORS_CREATOR_AFFILIATION(new FieldPath("Data File Creators", "Creator Affiliation")),
    DATA_FILE_CREATORS_CREATOR_ROLE(new FieldPath("Data File Creators", "Creator Role")),
    // Data File Parent Studies paths
    DATA_FILE_PARENT_STUDIES_STUDY_IDENTIFIER(new FieldPath("Data File Parent Studies", "Study Identifier")),
    DATA_FILE_PARENT_STUDIES_STUDY_NAME(new FieldPath("Data File Parent Studies", "Study Name")),
    // Data File Funding Sources paths
    DATA_FILE_FUNDING_SOURCES_AWARD_LOCAL_IDENTIFIER(new FieldPath("Data File Funding Sources", "Award Local Identifier")),
    DATA_FILE_FUNDING_SOURCES_FUNDER_NAME(new FieldPath("Data File Funding Sources", "Funder Name")),
    DATA_FILE_FUNDING_SOURCES_FUNDER_IDENTIFIER(new FieldPath("Data File Funding Sources", "Funder Identifier"));
    private final FieldPath path;
    private static final Set<String> recommendedFields = new HashSet<>();
    static {
        for (var recommendedField : values()) {
            recommendedFields.add(recommendedField.getPath().getPath());
        }
    }
    RecommendedFields(FieldPath path) {
        this.path = path;
    }
    public FieldPath getPath() {
        return path;
    }
    public static boolean isRecommendedField(String path) {
        return recommendedFields.contains(path);
    }
}
