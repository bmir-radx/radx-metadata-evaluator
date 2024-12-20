package bmir.radx.metadata.evaluator.util;

import bmir.radx.metadata.evaluator.SpreadsheetHeaders;
import bmir.radx.metadata.evaluator.StudyTemplateFields;

import java.util.HashMap;
import java.util.Map;

public class StudyHeaderConverter {
  private static final Map<String, String> codeListToFieldMap = new HashMap<>();
  private static final Map<String, SpreadsheetHeaders> rowFieldToHeaderMap = new HashMap<>();
  private static final Map<StudyTemplateFields, SpreadsheetHeaders> templateFieldToHeaderMap = new HashMap<>();
  private static final Map<String, StudyTemplateFields> rowFieldToTemplateFieldMap = new HashMap<>();


  static {
    // Code list headers to StudyMetadataRow fields
    codeListToFieldMap.put("HAS DATA FILES?", "hasDataFiles");
    codeListToFieldMap.put("MULTI-CENTER STUDY?", "multiCenterStudy");
    codeListToFieldMap.put("STUDY PROGRAM", "studyProgram");
    codeListToFieldMap.put("STUDY DESIGN", "studyDesign");
    codeListToFieldMap.put("DATA TYPES", "dataTypes");
    codeListToFieldMap.put("DATA_TYPES", "dataTypes");
    codeListToFieldMap.put("DATA COLLECTION METHODS", "dataCollectionMethod");
    codeListToFieldMap.put("SPECIES", "species");
    codeListToFieldMap.put("NIH INSTITUTE OR CENTER", "nihInstituteOrCenter");
    codeListToFieldMap.put("CONSENT/DATA USE LIMITATIONS", "consentDataUseLimitations");
    codeListToFieldMap.put("STUDY DOMAIN", "studyDomain");
    codeListToFieldMap.put("STUDY POPULATION FOCUS", "studyPopulationFocus");

    // StudyMetadataRow field name to SpreadsheetHeaders, using when report issue column
    rowFieldToHeaderMap.put("studyProgram", SpreadsheetHeaders.STUDY_PROGRAM);
    rowFieldToHeaderMap.put("studyPHS", SpreadsheetHeaders.STUDY_PHS);
    rowFieldToHeaderMap.put("studyTitle", SpreadsheetHeaders.STUDY_TITLE);
    rowFieldToHeaderMap.put("description", SpreadsheetHeaders.DESCRIPTION);
    rowFieldToHeaderMap.put("radxAcknowledgements", SpreadsheetHeaders.RADX_ACKNOWLEDGEMENTS);
    rowFieldToHeaderMap.put("nihGrantNumber", SpreadsheetHeaders.NIH_GRANT_NUMBER);
    rowFieldToHeaderMap.put("studyStartDate", SpreadsheetHeaders.STUDY_START_DATE);
    rowFieldToHeaderMap.put("studyEndDate", SpreadsheetHeaders.STUDY_END_DATE);
    rowFieldToHeaderMap.put("studyReleaseDate", SpreadsheetHeaders.STUDY_RELEASE_DATE);
    rowFieldToHeaderMap.put("updatedAt", SpreadsheetHeaders.UPDATED_AT);
    rowFieldToHeaderMap.put("foaNumber", SpreadsheetHeaders.FOA_NUMBER);
    rowFieldToHeaderMap.put("foaUrl", SpreadsheetHeaders.FOA_URL);
    rowFieldToHeaderMap.put("contactPiProjectLeader", SpreadsheetHeaders.CONTACT_PI_PROJECT_LEADER);
    rowFieldToHeaderMap.put("studyDoi", SpreadsheetHeaders.STUDY_DOI);
    rowFieldToHeaderMap.put("publicationUrls", SpreadsheetHeaders.PUBLICATION_URLS);
    rowFieldToHeaderMap.put("clinicalTrialsGovUrl", SpreadsheetHeaders.CLINICALTRIALS_GOV_URL);
    rowFieldToHeaderMap.put("studyWebsiteUrl", SpreadsheetHeaders.STUDY_WEBSITE_URL);
    rowFieldToHeaderMap.put("studyDesign", SpreadsheetHeaders.STUDY_DESIGN);
    rowFieldToHeaderMap.put("dataTypes", SpreadsheetHeaders.DATA_TYPES);
    rowFieldToHeaderMap.put("studyDomain", SpreadsheetHeaders.STUDY_DOMAIN);
    rowFieldToHeaderMap.put("nihInstituteOrCenter", SpreadsheetHeaders.NIH_INSTITUTE_OR_CENTER);
    rowFieldToHeaderMap.put("multiCenterStudy", SpreadsheetHeaders.MULTI_CENTER_STUDY);
    rowFieldToHeaderMap.put("multiCenterSites", SpreadsheetHeaders.MULTI_CENTER_SITES);
    rowFieldToHeaderMap.put("keywords", SpreadsheetHeaders.KEYWORDS);
    rowFieldToHeaderMap.put("dataCollectionMethod", SpreadsheetHeaders.DATA_COLLECTION_METHOD);
    rowFieldToHeaderMap.put("estimatedCohortSize", SpreadsheetHeaders.ESTIMATED_COHORT_SIZE);
    rowFieldToHeaderMap.put("studyPopulationFocus", SpreadsheetHeaders.STUDY_POPULATION_FOCUS);
    rowFieldToHeaderMap.put("species", SpreadsheetHeaders.SPECIES);
    rowFieldToHeaderMap.put("consentDataUseLimitations", SpreadsheetHeaders.CONSENT_DATA_USE_LIMITATIONS);
    rowFieldToHeaderMap.put("studyStatus", SpreadsheetHeaders.HAS_DATA_FILES); // Adjusted header
    rowFieldToHeaderMap.put("hasDataFiles", SpreadsheetHeaders.HAS_DATA_FILES);
    rowFieldToHeaderMap.put("diseaseSpecificGroup", SpreadsheetHeaders.DISEASE_SPECIFIC_GROUP);
    rowFieldToHeaderMap.put("diseaseSpecificRelatedConditions", SpreadsheetHeaders.DISEASE_SPECIFIC_RELATED_CONDITIONS);
    rowFieldToHeaderMap.put("healthBiomedGroup", SpreadsheetHeaders.HEALTH_BIOMED_GROUP);
    rowFieldToHeaderMap.put("studyCitation", SpreadsheetHeaders.STUDY_CITATION);
    rowFieldToHeaderMap.put("actualStudySize", SpreadsheetHeaders.ACTUAL_STUDY_SIZE);
    rowFieldToHeaderMap.put("studyVersion", SpreadsheetHeaders.STUDY_VERSION);
    rowFieldToHeaderMap.put("estimatedParticipantRange", SpreadsheetHeaders.ESTIMATED_PARTICIPANT_RANGE);
    rowFieldToHeaderMap.put("createdAt", SpreadsheetHeaders.CREATED_AT);

    //StudyTemplateField to SpreadsheetHeader
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_PROGRAM, SpreadsheetHeaders.STUDY_PROGRAM);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_PHS, SpreadsheetHeaders.STUDY_PHS);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_TITLE, SpreadsheetHeaders.STUDY_TITLE);
    templateFieldToHeaderMap.put(StudyTemplateFields.DESCRIPTION, SpreadsheetHeaders.DESCRIPTION);
    templateFieldToHeaderMap.put(StudyTemplateFields.RADX_ACKNOWLEDGEMENTS, SpreadsheetHeaders.RADX_ACKNOWLEDGEMENTS);
    templateFieldToHeaderMap.put(StudyTemplateFields.NIH_GRANT_NUMBERS, SpreadsheetHeaders.NIH_GRANT_NUMBER);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_DOI, SpreadsheetHeaders.STUDY_DOI);
    templateFieldToHeaderMap.put(StudyTemplateFields.PRINCIPAL_INVESTIGATOR, SpreadsheetHeaders.CONTACT_PI_PROJECT_LEADER);
    templateFieldToHeaderMap.put(StudyTemplateFields.FOA_NUMBERS, SpreadsheetHeaders.FOA_NUMBER);
    templateFieldToHeaderMap.put(StudyTemplateFields.FOA_URLS, SpreadsheetHeaders.FOA_URL);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_START_DATE, SpreadsheetHeaders.STUDY_START_DATE);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_END_DATE, SpreadsheetHeaders.STUDY_END_DATE);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_RELEASE_DATE, SpreadsheetHeaders.STUDY_RELEASE_DATE);
    templateFieldToHeaderMap.put(StudyTemplateFields.UPDATED_DATE, SpreadsheetHeaders.UPDATED_AT);
    templateFieldToHeaderMap.put(StudyTemplateFields.PUBLICATION_URLS, SpreadsheetHeaders.PUBLICATION_URLS);
    templateFieldToHeaderMap.put(StudyTemplateFields.CLINICALTRIALS_GOV_URLS, SpreadsheetHeaders.CLINICALTRIALS_GOV_URL);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_WEBSITE_URLS, SpreadsheetHeaders.STUDY_WEBSITE_URL);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_DESIGN, SpreadsheetHeaders.STUDY_DESIGN);
    templateFieldToHeaderMap.put(StudyTemplateFields.DATA_TYPES, SpreadsheetHeaders.DATA_TYPES);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_DOMAINS, SpreadsheetHeaders.STUDY_DOMAIN);
    templateFieldToHeaderMap.put(StudyTemplateFields.NIH_INSTITUTES_OR_CENTERS, SpreadsheetHeaders.NIH_INSTITUTE_OR_CENTER);
    templateFieldToHeaderMap.put(StudyTemplateFields.MULTI_CENTER_STUDY, SpreadsheetHeaders.MULTI_CENTER_STUDY);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_SITES, SpreadsheetHeaders.MULTI_CENTER_SITES);
    templateFieldToHeaderMap.put(StudyTemplateFields.KEYWORDS, SpreadsheetHeaders.KEYWORDS);
    templateFieldToHeaderMap.put(StudyTemplateFields.DATA_COLLECTION_METHODS, SpreadsheetHeaders.DATA_COLLECTION_METHOD);
    templateFieldToHeaderMap.put(StudyTemplateFields.ESTIMATED_SAMPLE_SIZE, SpreadsheetHeaders.ESTIMATED_COHORT_SIZE);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_POPULATION_FOCUS, SpreadsheetHeaders.STUDY_POPULATION_FOCUS);
    templateFieldToHeaderMap.put(StudyTemplateFields.SPECIES, SpreadsheetHeaders.SPECIES);
    templateFieldToHeaderMap.put(StudyTemplateFields.CONSENT_OR_DATA_USE_LIMITATIONS, SpreadsheetHeaders.CONSENT_DATA_USE_LIMITATIONS);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_STATUS, SpreadsheetHeaders.HAS_DATA_FILES);
    templateFieldToHeaderMap.put(StudyTemplateFields.DISEASE_SPECIFIC_GROUP, SpreadsheetHeaders.DISEASE_SPECIFIC_GROUP);
    templateFieldToHeaderMap.put(StudyTemplateFields.DISEASE_SPECIFIC_RELATED_CONDITIONS, SpreadsheetHeaders.DISEASE_SPECIFIC_RELATED_CONDITIONS);
    templateFieldToHeaderMap.put(StudyTemplateFields.HEALTH_BIOMED_GROUP, SpreadsheetHeaders.HEALTH_BIOMED_GROUP);
    templateFieldToHeaderMap.put(StudyTemplateFields.CITATION, SpreadsheetHeaders.STUDY_CITATION);
    templateFieldToHeaderMap.put(StudyTemplateFields.STUDY_SIZE, SpreadsheetHeaders.ACTUAL_STUDY_SIZE);
    templateFieldToHeaderMap.put(StudyTemplateFields.VERSION_NUMBER, SpreadsheetHeaders.STUDY_VERSION);
    templateFieldToHeaderMap.put(StudyTemplateFields.COHORT_SIZE_RANGE, SpreadsheetHeaders.ESTIMATED_PARTICIPANT_RANGE);
    templateFieldToHeaderMap.put(StudyTemplateFields.CREATION_DATE, SpreadsheetHeaders.CREATED_AT);

    //StudyMetadataRow to StudyTemplateField
    rowFieldToTemplateFieldMap.put("studyProgram", StudyTemplateFields.STUDY_PROGRAM);
    rowFieldToTemplateFieldMap.put("studyPHS", StudyTemplateFields.STUDY_PHS);
    rowFieldToTemplateFieldMap.put("studyTitle", StudyTemplateFields.STUDY_TITLE);
    rowFieldToTemplateFieldMap.put("description", StudyTemplateFields.DESCRIPTION);
    rowFieldToTemplateFieldMap.put("radxAcknowledgements", StudyTemplateFields.RADX_ACKNOWLEDGEMENTS);
    rowFieldToTemplateFieldMap.put("nihGrantNumber", StudyTemplateFields.NIH_GRANT_NUMBERS);
    rowFieldToTemplateFieldMap.put("studyStartDate", StudyTemplateFields.STUDY_START_DATE);
    rowFieldToTemplateFieldMap.put("studyEndDate", StudyTemplateFields.STUDY_END_DATE);
    rowFieldToTemplateFieldMap.put("studyReleaseDate", StudyTemplateFields.STUDY_RELEASE_DATE);
    rowFieldToTemplateFieldMap.put("updatedAt", StudyTemplateFields.UPDATED_DATE);
    rowFieldToTemplateFieldMap.put("foaNumber", StudyTemplateFields.FOA_NUMBERS);
    rowFieldToTemplateFieldMap.put("foaUrl", StudyTemplateFields.FOA_URLS);
    rowFieldToTemplateFieldMap.put("contactPiProjectLeader", StudyTemplateFields.PRINCIPAL_INVESTIGATOR);
    rowFieldToTemplateFieldMap.put("studyDoi", StudyTemplateFields.STUDY_DOI);
    rowFieldToTemplateFieldMap.put("publicationUrls", StudyTemplateFields.PUBLICATION_URLS);
    rowFieldToTemplateFieldMap.put("clinicalTrialsGovUrl", StudyTemplateFields.CLINICALTRIALS_GOV_URLS);
    rowFieldToTemplateFieldMap.put("studyWebsiteUrl", StudyTemplateFields.STUDY_WEBSITE_URLS);
    rowFieldToTemplateFieldMap.put("studyDesign", StudyTemplateFields.STUDY_DESIGN);
    rowFieldToTemplateFieldMap.put("dataTypes", StudyTemplateFields.DATA_TYPES);
    rowFieldToTemplateFieldMap.put("studyDomain", StudyTemplateFields.STUDY_DOMAINS);
    rowFieldToTemplateFieldMap.put("nihInstituteOrCenter", StudyTemplateFields.NIH_INSTITUTES_OR_CENTERS);
    rowFieldToTemplateFieldMap.put("multiCenterStudy", StudyTemplateFields.MULTI_CENTER_STUDY);
    rowFieldToTemplateFieldMap.put("multiCenterSites", StudyTemplateFields.STUDY_SITES);
    rowFieldToTemplateFieldMap.put("keywords", StudyTemplateFields.KEYWORDS);
    rowFieldToTemplateFieldMap.put("dataCollectionMethod", StudyTemplateFields.DATA_COLLECTION_METHODS);
    rowFieldToTemplateFieldMap.put("estimatedCohortSize", StudyTemplateFields.ESTIMATED_SAMPLE_SIZE);
    rowFieldToTemplateFieldMap.put("studyPopulationFocus", StudyTemplateFields.STUDY_POPULATION_FOCUS);
    rowFieldToTemplateFieldMap.put("species", StudyTemplateFields.SPECIES);
    rowFieldToTemplateFieldMap.put("consentDataUseLimitations", StudyTemplateFields.CONSENT_OR_DATA_USE_LIMITATIONS);
    rowFieldToTemplateFieldMap.put("studyStatus", StudyTemplateFields.STUDY_STATUS);
    rowFieldToTemplateFieldMap.put("hasDataFiles", StudyTemplateFields.HAS_DATA_FILES);
    rowFieldToTemplateFieldMap.put("diseaseSpecificGroup", StudyTemplateFields.DISEASE_SPECIFIC_GROUP);
    rowFieldToTemplateFieldMap.put("diseaseSpecificRelatedConditions", StudyTemplateFields.DISEASE_SPECIFIC_RELATED_CONDITIONS);
    rowFieldToTemplateFieldMap.put("healthBiomedGroup", StudyTemplateFields.HEALTH_BIOMED_GROUP);
    rowFieldToTemplateFieldMap.put("studyCitation", StudyTemplateFields.CITATION);
    rowFieldToTemplateFieldMap.put("actualStudySize", StudyTemplateFields.STUDY_SIZE);
    rowFieldToTemplateFieldMap.put("studyVersion", StudyTemplateFields.VERSION_NUMBER);
    rowFieldToTemplateFieldMap.put("estimatedParticipantRange", StudyTemplateFields.COHORT_SIZE_RANGE);
    rowFieldToTemplateFieldMap.put("createdAt", StudyTemplateFields.CREATION_DATE);
  }

  public static String convertCodeListHeaderToField(String codeListHeader) {
    return codeListToFieldMap.getOrDefault(codeListHeader, null);
  }

  public static SpreadsheetHeaders convertRowFieldToSpreadsheetHeader(String fieldName) {
    return rowFieldToHeaderMap.get(fieldName);
  }

  public static SpreadsheetHeaders convertTemplateFieldToSpreadsheetHeader(String field) {
    return templateFieldToHeaderMap.get(StudyTemplateFields.fromHeaderName(field));
  }

  public static StudyTemplateFields convertRowFieldToTemplateField(String fieldName) {
    return rowFieldToTemplateFieldMap.getOrDefault(fieldName, null);
  }
}
