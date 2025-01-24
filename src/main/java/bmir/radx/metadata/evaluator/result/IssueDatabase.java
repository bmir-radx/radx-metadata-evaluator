package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.MetadataEntity;
import bmir.radx.metadata.evaluator.SpreadsheetHeaders;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bmir.radx.metadata.evaluator.MetadataEntity.DATA_FILE_METADATA;
import static bmir.radx.metadata.evaluator.MetadataEntity.STUDY_METADATA;

public class IssueDatabase {
  private static final String STUDY_METADATA_FILE_LOC = "https://github.com/bmir-radx/radx-data-hub-metadata/blob/main/%20Study%20Metadata/RADx_studies_metadata_01222025.xlsx";
  private static final String FILE_METADATA_LOC_PREFIX = "https://github.com/bmir-radx/radx-data-hub-metadata/blob/main/Data%20File%20Metadata/metadata_dump_12122024/";
  private static final String EVALUATION_TOOL = "RADx Data Hub Metadata Evaluator";
  public record IssueDatabaseRecord(
      String issueId,
      String phs,
      String fileLocation,
      String fileLocationComments,
      String entityType,
      String metadataField,
      Integer startPosition,
      Integer endPosition,
      String issueType,
      String issueDescription,
      String issueLevel,
      String originValue,
      String repairSuggestion,
      LocalDate evaluationDate,
      String issueCreator,
      String hasFixed,
      String appliedToHub,
      String comments
  ) {}

  // Method to convert a SpreadsheetValidationResult to an IssueRecord
  public static IssueDatabaseRecord convertToIssueDatabase(SpreadsheetValidationResult s) {
    return new IssueDatabaseRecord(
        s.uuid(),                                 // Issue ID
        s.studyPhs(),                             // PHS
        STUDY_METADATA_FILE_LOC,                  // File location (example value, modify as needed)
        "Row " + s.row(),                        // File location comments
        STUDY_METADATA.getEntityName(),           // Entity Type
        s.column(),                               // Metadata Field
        getStartPosition(s.value()),   // Start Position
        getEndPosition(s.value()),     // End Position
        s.issueType().getName(),                  // Issue Type
        s.errorMessage(),                         // Issue Description (example value, modify as needed)
        s.issueLevel().getLevel(),                    // Issue Level
        s.value() != null ? s.value().toString() : "", // Original Value
        s.repairSuggestion(),                     // Repair Suggestion
        LocalDate.now(),                          // Evaluation Date
        EVALUATION_TOOL,                          // Issue Creator (example value, modify as needed)
        "No",                                     // Has Fixed
        "No",                                     // Applied to the Hub
        ""                                        // Comments (example value, modify as needed)
    );
  }

  // Method to convert a JsonValidationResult to an IssueRecord
  public static IssueDatabaseRecord convertToIssueDatabase(JsonValidationResult s) {
    if(s.pointer().equals(SpreadsheetHeaders.ESTIMATED_COHORT_SIZE.getHeaderName())){
      return convertSampleSizeResult(s);
    } else {
      return convertGenericResult(s);
    }
  }

  private static IssueDatabaseRecord convertGenericResult(JsonValidationResult s){
    return new IssueDatabaseRecord(
        s.uuid(),                                 // Issue ID
        s.studyPhs(),                             // PHS
        getFileLoc(s.studyPhs(), s.fileName()),
        "NA",                   // File location comments
        DATA_FILE_METADATA.getEntityName(),       // Entity Type
        s.pointer(),                              // Metadata Field
        getStartPosition(s.value()),              // Start Position
        getEndPosition(s.value()),                // End Position
        s.issueType().getName(),                  // Issue Type
        s.errorMessage(),                         // Issue Description
        s.issueLevel().getLevel(),                    // Issue Level
        s.value(),
        s.suggestion(),                           // Repair Suggestion
        LocalDate.now(),                          // Evaluation Date
        EVALUATION_TOOL,                          // Issue Creator (example value, modify as needed)
        "No",                                     // Has Fixed
        "No",                                     // Applied to the Hub
        ""                                        // Comments (example value, modify as needed)
    );
  }

  private static IssueDatabaseRecord convertSampleSizeResult(JsonValidationResult s){
    return new IssueDatabaseRecord(
        s.uuid(),                                 // Issue ID
        s.studyPhs(),                             // PHS
        STUDY_METADATA_FILE_LOC,                  // File location (example value, modify as needed)
        "Row " + s.suggestion(),                  // File location comments (row was put in suggestion)
        STUDY_METADATA.getEntityName(),           // Entity Type
        s.pointer(),                              // Metadata Field
        getStartPosition(s.value()),              // Start Position
        getEndPosition(s.value()),                // End Position
        s.issueType().getName(),                  // Issue Type
        s.errorMessage(),                         // Issue Description
        s.issueLevel().getLevel(),                    // Issue Level
        s.value(),
        null,                           // Repair Suggestion is null for sample size issue
        LocalDate.now(),                          // Evaluation Date
        EVALUATION_TOOL,                          // Issue Creator (example value, modify as needed)
        "No",                                     // Has Fixed
        "No",                                     // Applied to the Hub
        ""                                        // Comments (example value, modify as needed)
    );
  }


  private static Integer getEndPosition(Object value){
    if (value == null || value.equals("")){
      return null;
    }
    return value.toString().length() - 1;
  }

  private static Integer getStartPosition(Object value){
    if(getEndPosition(value) != null){
      return 0;
    }
    return null;
  }

  private static String getFileLoc(String phs, String fileName){
    var pattern = Pattern.compile("phs\\d+");
    var cleanPhs = pattern.matcher(phs);
    if(!cleanPhs.find()){
      System.out.println(phs + ": " + fileName);
    }
    return FILE_METADATA_LOC_PREFIX + cleanPhs.group() + "/" + fileName;
  }
}
