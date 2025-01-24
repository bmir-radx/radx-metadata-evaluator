package bmir.radx.metadata.evaluator;

public enum EvaluationMetric {
  ACCESSIBLE_URI_COUNT("Accessible URI Count"),
  ACCURACY_RATE("Accuracy Rate"),
  CONSISTENT_RECORD_RATE("Consistent Record Rate"),
  CONTROLLED_TERMS_FREQUENCY("Controlled Terms Frequency"),
  CONTROLLED_TERMS_DISTRIBUTION("Controlled Terms Distribution"),
  DUPLICATE_ELEMENT_INSTANCES("Duplicate Element Instances"),
  DUPLICATE_ELEMENT_INSTANCES_COUNT("Duplicate Element Instances Count"),
  DUPLICATE_RECORDS("Duplicate Records"),
  DUPLICATE_RECORDS_NUMBER("Duplicate Records Number"),
  ELEMENT_COMPLETION_RATE("Element Completion Rate"),
  ERROR("Error"),
  ERRORS_NUMBER("Errors Number"),
  ESTIMATED_ACCURATE_STUDY_RATE("Accurate Study Rate"),
  ESTIMATED_INACCURATE_STUDIES("Inaccurate Studies"),
  NUMBER_OF_ESTIMATED_INACCURATE_STUDIES("Number of Inaccurate Studies"),
  FILLED_CONTROLLED_TERMS_COUNT_DISTRIBUTION("Filled Controlled Terms Count"),
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
  INACCESSIBLE_RECORDS("Inaccessible Records"),
  INACCURATE_DATA_FILES("Inaccurate data files"),
  INCOMPLETE_STUDY_ROWS("Incomplete Study Rows"),
  INCOMPLETE_VARIABLES_ROWS("Incomplete Variables Rows"),
  INCONSISTENT_FILE_COUNT("Inconsistent File Count"),
  INCONSISTENT_FILE_COUNT_ROWS("Inconsistent File Count Rows"),
  INCONSISTENT_RECORDS("Inconsistent Records"),
  INCONSISTENT_STUDIES("Inconsistent Studies"),
  INCORRECT_CORE_CDES_ROWS("Incorrect Core CDEs Rows"),
  INVALID_DATA_FILE_METADATA("Invalid Data File Metadata"),
  INVALID_STUDIES("Invalid Studies"),
  NUMBER_OF_DUPLICATE_RECORDS("Number of Duplicate Records"),
  NUMBER_OF_INACCESSIBLE_RECORDS("Number of Inaccessible Records"),
  NUMBER_OF_INACCURATE_RECORDS("Number of Inaccurate Records"),
  NUMBER_OF_INCONSISTENT_RECORDS("Number of Inconsistent Records"),
  NUMBER_OF_INCORRECT_CORE_CDES("Number Of Incorrect Core CDEs"),
  NUMBER_OF_INVALID_RECORDS("Number of Invalid RECORDS"),
  NUMBER_OF_TIER_1_CDES("Number Of Tier 1 CDEs"),
  NUMBER_OF_VALIDATION_ERRORS("Number of Validation Errors"),
  OPTIONAL_FIELDS_COMPLETENESS("Optional Fields Completeness"),
  OPTIONAL_FIELDS_COMPLETENESS_DISTRIBUTION("Optional Fields Completeness Distribution"),
  OPTIONAL_FIELDS_PER_RECORD("Optional Fields Per Record"),
  OVERALL_COMPLETENESS("Overall Completeness"),
  OVERALL_COMPLETENESS_DISTRIBUTION("Overall Completeness Distribution"),
  RECOMMENDED_FIELDS_COMPLETENESS("Recommended Fields Completeness"),
  RECOMMENDED_FIELDS_COMPLETENESS_DISTRIBUTION("Recommended Fields Completeness Distribution"),
  REQUIRED_FIELDS_COMPLETENESS("Required Fields Completeness"),
  REQUIRED_FIELDS_COMPLETENESS_DISTRIBUTION("Required Fields Completeness Distribution"),
  RESOLVABLE_URL_RATE("Resolvable URL Rate"),
  TOTAL_FIELDS_PER_RECORD("Total Fields Per Record"),
  TOTAL_FILLED_FIELDS("Total Filled Fields"),
  TOTAL_NUMBER_OF_RECORDS("Total Number Of Records"),
  TOTAL_NUMBER_OF_VARIABLES("Total Number Of Variables"),
  RECOMMENDED_FIELDS_PER_RECORD("Recommended Fields Per Record"),
  REQUIRED_FIELDS_PER_RECORD("Required Fields Per Record"),
  UNIQUENESS_RATE("Uniqueness Rate"),
  URL_COUNT_DISTRIBUTION("URL Count Distribution"),
  VALIDATION_ERROR("Validation Error"),
  VALIDATION_PASS_RATE("Validation Pass Rate");


  private final String displayName;

  EvaluationMetric(String displayName) {
    this.displayName = displayName;
  }

  public String getDisplayName() {
    return displayName;
  }
}
