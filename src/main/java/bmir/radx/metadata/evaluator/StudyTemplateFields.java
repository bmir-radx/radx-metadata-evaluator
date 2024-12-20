package bmir.radx.metadata.evaluator;

public enum StudyTemplateFields implements Header{
  STUDY_PROGRAM("Study Program"),
  STUDY_PHS("Study PHS"),
  STUDY_TITLE("Study Title"),
  DESCRIPTION("Description"),
  RADX_ACKNOWLEDGEMENTS("RADx Acknowledgements"),
  NIH_GRANT_NUMBERS("NIH Grant Numbers"),
  STUDY_DOI("Study DOI"),
  PRINCIPAL_INVESTIGATOR("Principal Investigator"),
  FOA_NUMBERS("FOA Numbers"),
  FOA_URLS("FOA URLs"),
  RAPIDS_LINK("RAPIDS Link"),
  STUDY_START_DATE("Study Start Date"),
  STUDY_END_DATE("Study End Date"),
  STUDY_RELEASE_DATE("Study Release Date"),
  UPDATED_DATE("Updated Date"),
  PUBLICATION_URLS("Publication URLs"),
  CLINICALTRIALS_GOV_URLS("ClinicalTrials.gov URLs"),
  STUDY_WEBSITE_URLS("Study Website URLs"),
  STUDY_DESIGN("Study Design"),
  DATA_TYPES("Data Types"),
  STUDY_DOMAINS("Study Domains"),
  NIH_INSTITUTES_OR_CENTERS("NIH Institutes Or Centers"),
  MULTI_CENTER_STUDY("Multi-Center Study"),
  STUDY_SITES("Study Sites"),
  KEYWORDS("Keywords"),
  DATA_COLLECTION_METHODS("Data Collection Methods"),
  ESTIMATED_SAMPLE_SIZE("Estimated Sample Size"),
  STUDY_POPULATION_FOCUS("Study Population Focus"),
  SPECIES("Species"),
  CONSENT_OR_DATA_USE_LIMITATIONS("Consent Or Data Use Limitations"),
  STUDY_STATUS("Study Status"),
  HAS_DATA_FILES("Has Data Files"),
  DISEASE_SPECIFIC_GROUP("Disease Specific Group"),
  DISEASE_SPECIFIC_RELATED_CONDITIONS("Disease Specific Related Conditions"),
  HEALTH_BIOMED_GROUP("Health Biomed Group"),
  CITATION("Citation"),
  STUDY_SIZE("Study Size"),
  VERSION_NUMBER("Version Number"),
  COHORT_SIZE_RANGE("Cohort Size Range"),
  CREATION_DATE("Creation Date");



  private final String fieldTitle;

  StudyTemplateFields(String fieldTitle) {
    this.fieldTitle = fieldTitle;
  }

  public String getFieldTitle() {
    return fieldTitle;
  }

  public static StudyTemplateFields fromHeaderName(String headerName) {
    for (var field : values()) {
      if (field.fieldTitle.equals(headerName)) {
        return field;
      }
    }
    throw new IllegalArgumentException("No enum constant for header name: " + headerName);
  }

  public static String getFieldPath(StudyTemplateFields field){
    if(field.equals(CLINICALTRIALS_GOV_URLS)){
      return "clinicaltrialsUrl";
    }
    String rawPath = field.getFieldTitle()
        .replaceAll("[^a-zA-Z0-9 ]", "") // Remove special characters
        .replaceAll(" ", "");

    return "/" + Character.toLowerCase(rawPath.charAt(0)) + rawPath.substring(1);
  }
}
