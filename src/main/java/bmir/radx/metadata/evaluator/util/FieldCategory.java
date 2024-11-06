package bmir.radx.metadata.evaluator.util;

import java.util.Arrays;

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

  public static String[] getCategoryNames() {
    return Arrays.stream(FieldCategory.values())
        .map(FieldCategory::getCategory)
        .toArray(String[]::new);
  }

}

