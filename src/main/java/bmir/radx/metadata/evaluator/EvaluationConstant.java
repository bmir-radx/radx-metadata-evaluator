package bmir.radx.metadata.evaluator;

public enum EvaluationConstant {
  ACCESSIBLE_URI_COUNT("Accessible URI Count"),
  ACCURATE_STUDY_RATE("Accurate Study Rate"),
  CONSISTENT_STUDY_RATE("Consistent Study Rate"),
  CONTROLLED_TERMS_FREQUENCY("Controlled Terms Frequency"),
  CONTROLLED_TERMS_DISTRIBUTION("Controlled Terms Distribution"),
  DUPLICATE_ELEMENT_INSTANCES("Duplicate Element Instances"),
  DUPLICATE_ELEMENT_INSTANCES_COUNT("Duplicate Element Instances Count"),
  DUPLICATE_STUDIES("Duplicate Studies"),
  ELEMENT_COMPLETION_RATE("Element Completion Rate"),
  ERROR("Error"),
  ERRORS_NUMBER("Errors Number"),
  FILLED_CONTROLLED_TERMS_COUNT("Filled Controlled Terms Count"),
  FILLED_ELEMENTS("Filled Elements"),
  FILLED_ELEMENTS_COUNT("Filled Elements Count"),
  FILLED_OPTIONAL_FIELDS("Filled Optional Fields"),
  FILLED_OPTIONAL_FIELDS_COUNT("Filled Optional Fields Count"),
  FILLED_RECOMMENDED_FIELDS("Filled Recommended Fields"),
  FILLED_RECOMMENDED_FIELDS_COUNT("Filled Recommended Fields Count"),
  FILLED_REQUIRED_FIELDS("Filled Required Fields"),
  FILLED_REQUIRED_FIELDS_COUNT("Filled Required Fields Count"),
  FULL_COMPLETENESS_STUDY_RATIO("Full Completeness Study Ratio"),
  FULL_COMPLETENESS_VARIABLE_RATIO("Full Completeness Variable Ratio"),
  INACCURATE_STUDIES("Inaccurate Studies"),
  INCOMPLETE_STUDY_ROWS("Incomplete Study Rows"),
  INCOMPLETE_VARIABLES_ROWS("Incomplete Variables Rows"),
  INCONSISTENT_FILE_COUNT("Inconsistent File Count"),
  INCONSISTENT_FILE_COUNT_ROWS("Inconsistent File Count Rows"),
  INCONSISTENT_STUDIES("Inconsistent Studies"),
  INCORRECT_CORE_CDES_ROWS("Incorrect Core CDEs Rows"),
  INVALID_STUDIES("Invalid Studies"),
  NUMBER_OF_INACCURATE_STUDIES("Number of Inaccurate Studies"),
  NUMBER_OF_INCONSISTENT_STUDIES("Number of Inconsistent Studies"),
  NUMBER_OF_INCORRECT_CORE_CDES("Number Of Incorrect Core CDEs"),
  NUMBER_OF_INVALID_STUDIES("Number of Invalid Studies"),
  NUMBER_OF_TIER_1_CDES("Number Of Tier 1 CDEs"),
  NUMBER_OF_VALIDATION_ERRORS("Number of Validation Errors"),
  OPTIONAL_FIELDS_COMPLETENESS_DISTRIBUTION("Optional Fields Completeness Distribution"),
  OPTIONAL_FIELDS_COMPLETION_RATE("Optional Fields Completion Rate"),
  OVERALL_COMPLETENESS_DISTRIBUTION("Overall Completeness Distribution"),
  OVERALL_COMPLETION_RATE("Overall Completion Rate"),
  RECOMMENDED_FIELDS_COMPLETENESS_DISTRIBUTION("Recommended Fields Completeness Distribution"),
  RECOMMENDED_FIELDS_COMPLETION_RATE("Recommended Fields Completion Rate"),
  REQUIRED_FIELDS_COMPLETENESS_DISTRIBUTION("Required Fields Completeness Distribution"),
  REQUIRED_FIELDS_COMPLETION_RATE("Required Fields Completion Rate"),
  RESOLVABLE_URL_RATE("Resolvable URL Rate"),
  TOTAL_FIELDS("Total fields"),
  TOTAL_FILLED_FIELDS("Total Filled Fields"),
  TOTAL_NUMBER_OF_DATA_FILES("Total Number Of Data Files"),
  TOTAL_NUMBER_OF_STUDIES("Total Number Of Studies"),
  TOTAL_NUMBER_OF_VARIABLES("Total Number Of Variables"),
  TOTAL_OPTIONAL_FIELDS("Total Optional Fields"),
  TOTAL_RECOMMENDED_FIELDS("Total Recommended Fields"),
  TOTAL_REQUIRED_FIELDS("Total Required Fields"),
  UNIQUENESS("Uniqueness"),
  URL_COUNT_DISTRIBUTION("URL Count Distribution"),
  VALIDATION_ERROR("Validation Error"),

  VALIDATION_PASS_RATE("Validation Pass Rare");


  private final String displayName;

  EvaluationConstant(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
