package bmir.radx.metadata.evaluator;

public enum MetadataEntity {
  STUDY_METADATA("Study Metadata"),
  DATA_FILE_METADATA("Data File Metadata"),
  VARIABLE_METADATA("Variable Metadata");

  private final String entityName;

  MetadataEntity(String entityName) {
    this.entityName = entityName;
  }

  public String getEntityName() {
    return entityName;
  }
}
