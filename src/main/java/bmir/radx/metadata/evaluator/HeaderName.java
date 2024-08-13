package bmir.radx.metadata.evaluator;

public enum HeaderName {
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
  STUDY_PROGRAM("STUDY PROGRAM"),
  STUDY_PHS("STUDY PHS"),
  STUDY_TITLE("STUDY TITLE"),
  DESCRIPTION("DESCRIPTION"),
  RADX_ACKNOWLEDGEMENTS("RADX ACKNOWLEDGEMENTS"),
  NIH_GRANT_NUMBER("NIH GRANT NUMBER"),
  RAPIDS_LINK("RAPIDS LINK"),
  STUDY_START_DATE("STUDY START DATE"),
  STUDY_END_DATE("STUDY END DATE"),
  STUDY_RELEASE_DATE("STUDY RELEASE DATE"),
  UPDATED_AT("UPDATED AT"),
  FOA_NUMBER("FOA NUMBER"),
  FOA_URL("FOA URL"),
  CONTACT_PI_PROJECT_LEADER("CONTACT PI/PROJECT LEADER"),
  STUDY_DOI("STUDY DOI"),
  DCC_PROVIDED_PUBLICATION_URLS("(C)DCC-PROVIDED PUBLICATION URL(S)"),
  CLINICALTRIALS_GOV_URL("CLINICALTRIALS.GOV URL"),
  STUDY_WEBSITE_URL("STUDY WEBSITE URL"),
  STUDY_DESIGN("STUDY DESIGN"),
  DATA_TYPES("DATA TYPES"),
  STUDY_DOMAIN("STUDY DOMAIN"),
  NIH_INSTITUTE_OR_CENTER("NIH INSTITUTE OR CENTER"),
  MULTI_CENTER_STUDY("MULTI-CENTER STUDY?"),
  MULTI_CENTER_SITES("MULTI-CENTER SITES"),
  KEYWORDS("KEYWORDS"),
  DATA_COLLECTION_METHOD("DATA COLLECTION METHOD"),
  ESTIMATED_COHORT_SIZE("ESTIMATED COHORT SIZE"),
  STUDY_POPULATION_FOCUS("STUDY POPULATION FOCUS"),
  SPECIES("SPECIES"),
  CONSENT_DATA_USE_LIMITATIONS("CONSENT/DATA USE LIMITATIONS"),
  STUDY_STATUS("STUDY STATUS"),
  HAS_DATA_FILES("HAS DATA FILES?");

  private final String headerName;

  HeaderName(String headerName) {
    this.headerName = headerName;
  }

  public String getHeaderName() {
    return headerName;
  }

  public static HeaderName fromHeaderName(String headerName) {
    for (var field : values()) {
      if (field.headerName.equals(headerName)) {
        return field;
      }
    }
    throw new IllegalArgumentException("No enum constant for header name: " + headerName);
  }
}
