package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.MetadataEntity;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static bmir.radx.metadata.evaluator.MetadataEntity.DATA_FILE_METADATA;
import static bmir.radx.metadata.evaluator.MetadataEntity.STUDY_METADATA;

public class IssueDatabase {
  private static final String STUDY_METADATA_FILE_LOC = "https://github.com/bmir-radx/radx-data-hub-metadata/tree/main/%20Study%20Metadata";
  private static final String FILE_METADATA_LOC_PREFIX = "https://github.com/bmir-radx/radx-data-hub-metadata/blob/main/Data%20File%20Metadata/metadata_dump_12122024/";
  private static final String EVALUATION_TOOL = "RADx Data Hub Metadata Evaluator";
  public record IssueDatabaseRecord(
      String issueId,
      String phs,
      String fileLocation,
      String fileLocationComments,
      String entityType,
      String metadataField,
      int startPosition,
      int endPosition,
      String issueType,
      String issueDescription,
      String issueLevel,
      String originValue,
      String repairSuggestion,
      LocalDate evaluationDate,
      String issueCreator,
      String comments
  ) {}

  // Method to convert a SpreadsheetValidationResult to an IssueRecord
  public static IssueDatabaseRecord convertToIssueDatabase(SpreadsheetValidationResult s) {
    return new IssueDatabaseRecord(
        s.uuid(),                                 // Issue ID
        s.studyPhs(),                            // PHS
        STUDY_METADATA_FILE_LOC,             // File location (example value, modify as needed)
        "Row: " + s.row(),                        // File location comments
        STUDY_METADATA.getEntityName(),                   // Entity Type
        s.column(),                               // Metadata Field
        0,                                        // Start Position (not available in SpreadsheetValidationResult)
        getEndPosition(s.value().toString()),                                        // End Position (not available in SpreadsheetValidationResult)
        s.issueType().getName(),                  // Issue Type
        s.errorMessage(),            // Issue Description (example value, modify as needed)
        s.issueLevel().name(),                    // Issue Level
        s.value() != null ? s.value().toString() : "", // Original Value
        s.repairSuggestion(),                     // Repair Suggestion
        LocalDate.now(),                          // Evaluation Date
        EVALUATION_TOOL,             // Issue Creator (example value, modify as needed)
        ""                                        // Comments (example value, modify as needed)
    );
  }

  // Method to convert a JsonValidationResult to an IssueRecord
  public static IssueDatabaseRecord convertToIssueDatabase(JsonValidationResult s) {
    return new IssueDatabaseRecord(
        s.uuid(),                                 // Issue ID
        s.studyPhs(),                             // PHS
        getFileLoc(s.studyPhs(), s.fileName()),
        "NA",                                    // File location comments
        DATA_FILE_METADATA.getEntityName(),                          // Entity Type
        s.pointer(),                              // Metadata Field
        0,                                        // Start Position (not available in JsonValidationResult)
        getEndPosition(s.value()),                                        // End Position (not available in JsonValidationResult)
        s.issueType().getName(),                  // Issue Type
        s.errorMessage(),                         // Issue Description
        s.issueLevel().name(),                    // Issue Level
        s.value(),
        s.suggestion(),                           // Repair Suggestion
        LocalDate.now(),                          // Evaluation Date
        EVALUATION_TOOL,                          // Issue Creator (example value, modify as needed)
        ""                                        // Comments (example value, modify as needed)
    );
  }

  private static int getEndPosition(String value){
    return value.length();
  }

  private static String getFileLoc(String phs, String fileName){
    var pattern = Pattern.compile("phs\\d+");
    var cleanPhs = pattern.matcher(phs);
    if(!cleanPhs.find()){
      System.err.println(phs);
    }
    return FILE_METADATA_LOC_PREFIX + cleanPhs.group() + "/" + fileName;
  }
}
