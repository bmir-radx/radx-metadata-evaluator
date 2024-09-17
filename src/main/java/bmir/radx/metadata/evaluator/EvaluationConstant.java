package bmir.radx.metadata.evaluator;

public enum EvaluationConstant {
  ERROR("Error"),
  TOTAL_FIELDS("Total fields"),
  TOTAL_REQUIRED_FIELDS("Total Required Fields"),
  TOTAL_RECOMMENDED_FIELDS("Total Recommended Fields"),
  TOTAL_OPTIONAL_FIELDS("Total Optional Fields"),
  FILLED_REQUIRED_FIELDS("Filled Required Fields"),
  FILLED_RECOMMENDED_FIELDS("Filled Recommended Fields"),
  FILLED_OPTIONAL_FIELDS("Filled Optional Fields"),
  FILLED_ELEMENTS("Filled Elements"),
  FILLED_ELEMENTS_COUNT("Filled Elements Count"),
  FILLED_REQUIRED_FIELDS_COUNT("Filled Required Fields Count"),
  FILLED_RECOMMENDED_FIELDS_COUNT("Filled Recommended Fields Count"),
  FILLED_OPTIONAL_FIELDS_COUNT("Filled Optional Fields Count"),
  REQUIRED_FIELDS_COMPLETION_RATE("Required Fields Completion Rate"),
  RECOMMENDED_FIELDS_COMPLETION_RATE("Recommended Fields Completion Rate"),
  OPTIONAL_FIELDS_COMPLETION_RATE("Optional Fields Completion Rate"),
  ELEMENT_COMPLETION_RATE("Element Completion Rate"),
  TOTAL_FIELDS_COUNT("Total Fields Count"),
  TOTAL_FILLED_FIELDS_COUNT("Total Filled Fields Count"),
  OVERALL_COMPLETION_RATE("Overall Completion Rate"),
  OVERALL_COMPLETENESS_DISTRIBUTION("Overall Completeness Distribution"),
  REQUIRED_FIELDS_COMPLETENESS_DISTRIBUTION("Required Field Completeness Distribution"),
  RECOMMENDED_FIELDS_COMPLETENESS_DISTRIBUTION("Recommended Field Completeness Distribution"),
  OPTIONAL_FIELDS_COMPLETENESS_DISTRIBUTION("Optional Fields Completeness Distribution"),
  FILLED_CONTROLLED_TERMS_COUNT("Filled Controlled Terms Count"),
  CONTROLLED_TERMS_FREQUENCY("Controlled Terms Frequency"),
  ACCESSIBLE_URI_COUNT("Accessible URI Count"),
  DUPLICATE_ELEMENT_INSTANCES_COUNT("Duplicate Element Instances Count"),
  DUPLICATE_ELEMENT_INSTANCES("Duplicate Element Instances"),
  VALIDATION_ERROR_COUNT("Validation Error Count"),
  VALIDATION_ERROR("Validation Error"),
  TOTAL_NUMBER_OF_VARIABLE("Total Number Of Variable"),
  FULL_COMPLETENESS_VARIABLE_RATIO("Full Completeness Variable Ratio"),
  INCOMPLETE_VARIABLES_ROWS("Incomplete Variables Rows"),
  NUMBER_OF_TIER_1_CDES("Number Of Tier 1 CDEs"),
  NUMBER_OF_INCORRECT_CORE_CDES("Number Of Incorrect Core CDEs"),
  INCORRECT_CORE_CDES_ROWS("Incorrect Core CDEs Rows"),
  INCONSISTENT_STUDY_COUNT("Inconsistent Study Count"),
  INCONSISTENT_FILE_COUNT("Inconsistent File Count"),
  INCONSISTENT_STUDY_COUNT_ROWS("Inconsistent Study Count Rows"),
  INCONSISTENT_FILE_COUNT_ROWS("Inconsistent File Count Rows"),
  FULL_COMPLETENESS_STUDY_RATIO("Full Completeness Study Ratio"),
  INCOMPLETE_STUDY_ROWS("Incomplete Study Rows");

  private final String displayName;

  EvaluationConstant(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
