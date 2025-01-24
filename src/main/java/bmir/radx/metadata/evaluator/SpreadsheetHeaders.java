package bmir.radx.metadata.evaluator;

public enum SpreadsheetHeaders implements Header{
  DATA_VARIABLE("Data Variable"),
  IS_TIER_1_CDE("Is Tier 1 CDE"),
  FILE_COUNT("File Count"),
  FILE_NAME("File Name"),
  STUDY_COUNT("Study Count"),
  STUDY_NAME("Study Name"),
  DB_GAP_ID("dbGaP ID"),
  DB_GAP_IDS("dbGaP IDs"),
  FILES_PER_STUDY("Files Per Study"),
  RADX_PROGRAM("RADx Program"),
  LABEL("Label"),
  CONCEPT("Concept"),
  RESPONSES("Responses"),
  RADX_GLOBAL_PROMPT("RADx Global Prompt"),
  VARIABLE("Variable"),
  VARIABLES("Variables"),
  STUDY_PROGRAM("dcc"),
  STUDY_PHS("phs"),
  STUDY_TITLE("title"),
  DESCRIPTION("description"),
  RADX_ACKNOWLEDGEMENTS("acknowledgement_statement"),
  NIH_GRANT_NUMBER("grant_number"),
//  RAPIDS_LINK("RAPIDS_link"),
  STUDY_START_DATE("studystartdate"),
  STUDY_END_DATE("studyenddate"),
  STUDY_RELEASE_DATE("release_date"),
  UPDATED_AT("updated_at"),
  FOA_NUMBER("FOA_number"),
  FOA_URL("FOA_URL"),
  CONTACT_PI_PROJECT_LEADER("pi_name"),
  STUDY_DOI("study_DOI"),
  PUBLICATION_URLS("publication_URLs"),
  CLINICALTRIALS_GOV_URL("CT_URL"),
  STUDY_WEBSITE_URL("study_website_URL"),
  STUDY_DESIGN("types"),
  DATA_TYPES("data_general_types"),
  STUDY_DOMAIN("topics"),
  NIH_INSTITUTE_OR_CENTER("institutes_supporting_study"),
  MULTI_CENTER_STUDY("is_multi_center"),
  MULTI_CENTER_SITES("multi_center_sites"),
  KEYWORDS("subject"),
  DATA_COLLECTION_METHOD("source"),
  ESTIMATED_COHORT_SIZE("estimated_participants"),
  STUDY_POPULATION_FOCUS("study_population_focus"),
  SPECIES("data_species"),
  CONSENT_DATA_USE_LIMITATIONS("general_research_group"),
  HAS_DATA_FILES("has_data_files"),
  ESTIMATED_PARTICIPANT_RANGE("estimated_participant_range"),
  DISEASE_SPECIFIC_GROUP("disease_specific_group"),
  DISEASE_SPECIFIC_RELATED_CONDITIONS("disease_specific_related_conditions"),
  HEALTH_BIOMED_GROUP("health_biomed_group"),
  STUDY_CITATION("study_citation"),
  ACTUAL_STUDY_SIZE("actual_study_size"),
  STUDY_VERSION("study_version"),
  CREATED_AT("created_at");

  private final String headerName;

  SpreadsheetHeaders(String headerName) {
    this.headerName = headerName;
  }

  public String getHeaderName() {
    return headerName;
  }

  public static SpreadsheetHeaders fromHeaderName(String headerName) {
    for (var field : values()) {
      if (field.headerName.equals(headerName)) {
        return field;
      }
    }
    throw new IllegalArgumentException("No enum constant for header name: " + headerName);
  }
}
