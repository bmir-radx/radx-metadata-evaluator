package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.SpreadsheetReader;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.study.StudyMetadataRow;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.INACCURATE_FIELD;

@Component
public class StudyDataFileCrossEvaluator {
  private static final String DATA_FILE_PARENT_STUDIES = "Data File Parent Studies";
  private static final String STUDY_NAME = "Study Name";
  private static final String PHS_IDENTIFIER = "PHS Identifier";
  private static final String DATA_FILE_FUNDING_SOURCES = "Data File Funding Sources";
  private static final String AWARD_LOCAL_IDENTIFIER = "Award Local Identifier";
  private final SpreadsheetReader spreadsheetReader;

  public StudyDataFileCrossEvaluator(SpreadsheetReader spreadsheetReader) {
    this.spreadsheetReader = spreadsheetReader;
  }

  public void evaluate(Path studyPath,
                       Set<String> inaccurateStudies,
                       Map<Path, TemplateInstanceArtifact> dataFileRecords,
                       ValidationSummary<JsonValidationResult> validationSummary){
    var studyRecords = spreadsheetReader.getStudyMetadataMapping(studyPath);

    Map<String, String> dataFile2Study = spreadsheetReader.getDataFile2StudyMapping();

    for(var dataFileMetadata : dataFileRecords.entrySet()){
      var dataFilePath = dataFileMetadata.getKey();
      var dataFileName = dataFilePath.getFileName().toString();
      var dataFileArtifact = dataFileMetadata.getValue();

      if(dataFile2Study.containsKey(dataFileName)){
        var studyPhs = dataFile2Study.get(dataFileName);

        if(studyRecords.containsKey(studyPhs)){
          var studyArtifact = studyRecords.get(studyPhs);
          evaluateSinglePair(dataFileName, dataFileArtifact, studyArtifact, inaccurateStudies, validationSummary);
        } else{
          System.err.println(studyPhs + " is not exist in study metadata");
        }

      } else{
        System.err.println(dataFileName + " is not exist in bundles mappings");
      }

    }
  }

  private void evaluateSinglePair(String dataFileName,
                                  TemplateInstanceArtifact dataFileArtifact,
                                  StudyMetadataRow studyArtifact,
                                  Set<String> inaccurateStudies,
                                  ValidationSummary<JsonValidationResult> validationSummary){
    var studyPhs = studyArtifact.studyPHS();
    var studyName = studyArtifact.studyTitle();
    var studyGrandNumber = studyArtifact.nihGrantNumber();

    //todo: need to have a more robust way to retrieve value from data file metadata
    var parentStudiesArtifacts = dataFileArtifact.multiInstanceElementInstances().get(DATA_FILE_PARENT_STUDIES);
    var dataFileStudyName = parentStudiesArtifacts.get(0).singleInstanceFieldInstances().get(STUDY_NAME).jsonLdValue();
    var dataFileStudyPhs = parentStudiesArtifacts.get(0).singleInstanceFieldInstances().get(PHS_IDENTIFIER).jsonLdValue();

    var fundingSources = dataFileArtifact.multiInstanceElementInstances().get(DATA_FILE_FUNDING_SOURCES);
    var dataFileFundingId = fundingSources.get(0).singleInstanceFieldInstances().get(AWARD_LOCAL_IDENTIFIER).jsonLdValue();

    // Check PHS
    dataFileStudyPhs.ifPresent(s -> compareValues(studyPhs, dataFileName, studyPhs, s, DATA_FILE_PARENT_STUDIES, PHS_IDENTIFIER, inaccurateStudies, validationSummary));
    // Check Study Name
    dataFileStudyName.ifPresent(s -> compareValues(studyPhs, dataFileName, studyName, s, DATA_FILE_PARENT_STUDIES, STUDY_NAME, inaccurateStudies, validationSummary));
    // Check Award Local Identifier
    if(dataFileFundingId.isPresent()){
      var dataFileFundingIdString = dataFileFundingId.get().replaceAll(" ", "");
      compareValues(studyPhs, dataFileName, studyGrandNumber, dataFileFundingIdString, DATA_FILE_FUNDING_SOURCES, AWARD_LOCAL_IDENTIFIER, inaccurateStudies, validationSummary);
    }
  }

  private void compareValues(String studyPhs,
                             String dataFileName,
                             String studyMetadata,
                             String dataFileMetadata,
                             String element,
                             String field,
                             Set<String> inaccurateStudies,
                             ValidationSummary<JsonValidationResult> validationSummary){

    if(!dataFileMetadata.equals(studyMetadata)){
      inaccurateStudies.add(dataFileName);
      var pointer = getPointer(element, field);
      var errorMessage = getErrorMessage(field, dataFileMetadata);
      validationSummary.addInvalidMetadata(dataFileName);
      validationSummary.updateValidationResult(new JsonValidationResult(studyPhs, dataFileName, pointer, INACCURATE_FIELD, errorMessage, studyMetadata));
    }
  }

  private String getPointer(String element, String field){
    return element +
        "[0]/" +
        field;
  }

  private String getErrorMessage(String field, String value){
    return field + " [" + value + "] does not match the study metadata";
  }
}
