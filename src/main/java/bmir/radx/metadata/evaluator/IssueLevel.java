package bmir.radx.metadata.evaluator;

public enum IssueLevel {
  ERROR("Error"),
  INFO("Info"),
  REVIEW_NEEDED("Review Needed");

  private final String level;

  IssueLevel(String level) {
    this.level = level;
  }

  public String getLevel(){
    return level;
  }
}
