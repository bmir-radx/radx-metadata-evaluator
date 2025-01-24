package bmir.radx.metadata.evaluator.util;

public enum InstanceArtifactPath {
  STUDY_NAME_PATH("/Data File Parent Studies[0]/Study Name"),
  STUDY_PHS_PATH("/Data File Parent Studies[0]/PHS Identifier"),
  TITLE_PATH("/Data File Titles[0]/Title"),
  FILE_NAME_PATH("/Data File Identity/File Name"),
  AWARD_LOCAL_IDENTIFIER_PATH("/Data File Funding Sources[0]/Award Local Identifier"),
  DATA_CHAR_SUMMARY_PATH("/Data Characteristics Summary/Data Characteristics Table in HTML");

  private final String path;

  InstanceArtifactPath(String path) {
    this.path = path;
  }

  public String getPath() {
    return path;
  }
}
