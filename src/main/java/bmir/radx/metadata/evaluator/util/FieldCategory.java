package bmir.radx.metadata.evaluator.util;

public enum FieldCategory {
  REQUIRED("Required"),
  RECOMMENDED("Recommended"),
  OPTIONAL("Optional"),
  OVERALL("Overall");

  private final String category;

  FieldCategory(String category) {
    this.category = category;
  }

  public String getCategory() {
    return category;
  }
}

