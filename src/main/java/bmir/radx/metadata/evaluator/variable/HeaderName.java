package bmir.radx.metadata.evaluator.variable;

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
  VARIABLES("Variables");

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
