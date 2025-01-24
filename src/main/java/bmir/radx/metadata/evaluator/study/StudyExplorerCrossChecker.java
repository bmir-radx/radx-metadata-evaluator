package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.SpreadsheetHeaders;
import bmir.radx.metadata.evaluator.SpreadsheetReader;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import static bmir.radx.metadata.evaluator.SpreadsheetHeaders.*;

@Component
public class StudyExplorerCrossChecker {
  @Value("${study.explorer.results}")
  private String studyExplorerResultsFileName;
  private final SpreadsheetReader spreadsheetReader;

  public StudyExplorerCrossChecker(SpreadsheetReader spreadsheetReader) {
    this.spreadsheetReader = spreadsheetReader;
  }

  public void evaluate(Path studyMetadataPath, Path studyExplorerPath, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    try {
      var explorerResults = readCsvToStudyMetadataRows(studyExplorerPath);
      var metadataDump = spreadsheetReader.getStudyMetadataMapping(studyMetadataPath);

      for(var explorerResult: explorerResults){
        String phs = explorerResult.studyPHS();
        if(phs.equals("phs002682")){
          System.out.println("phs002682");
        }
        if(metadataDump.containsKey(phs)){
          compareFields(phs, explorerResult, metadataDump.get(phs), validationSummary);
        } else{
          System.err.println("PHS not found in dump: " + phs);
        }
      }
    } catch (IOException | ParseException e) {
      throw new RuntimeException(e);
    }
  }

  private void compareFields(String phs, StudyMetadataRow csvRow, StudyMetadataRow dumpRow, ValidationSummary<SpreadsheetValidationResult> validationSummary) {
    Map<SpreadsheetHeaders, Boolean> comparisonResults = new LinkedHashMap<>();

    // Direct comparisons
    comparisonResults.put(STUDY_TITLE, Objects.equals(csvRow.studyTitle(), dumpRow.studyTitle()));
    comparisonResults.put(ESTIMATED_COHORT_SIZE, Objects.equals(csvRow.estimatedCohortSize(), dumpRow.estimatedCohortSize()));
    comparisonResults.put(HAS_DATA_FILES, Objects.equals(csvRow.hasDataFiles(), dumpRow.hasDataFiles()));

    // Set comparisons for multiple values
    comparisonResults.put(STUDY_POPULATION_FOCUS, compareSets(csvRow.studyPopulationFocus(), dumpRow.studyPopulationFocus()));
    comparisonResults.put(STUDY_DOMAIN, compareSets(csvRow.studyDomain(), dumpRow.studyDomain()));
    comparisonResults.put(STUDY_DESIGN, compareSets(csvRow.studyDesign(), dumpRow.studyDesign()));
    comparisonResults.put(DATA_COLLECTION_METHOD, compareSets(csvRow.dataCollectionMethod(), dumpRow.dataCollectionMethod()));
    comparisonResults.put(NIH_INSTITUTE_OR_CENTER, compareSets(csvRow.nihInstituteOrCenter(), dumpRow.nihInstituteOrCenter()));

    for (var entry : comparisonResults.entrySet()) {
      if (!entry.getValue()) {
        var fieldName = entry.getKey();
        var errorMessage = String.format("The value on the Study Overview page [%s] does not align with the corresponding value on the Study Explorer page [%s].", getFieldValue(dumpRow, fieldName), getFieldValue(csvRow, fieldName));
        validationSummary.updateValidationResult(
            new SpreadsheetValidationResult(
                IssueTypeMapping.IssueType.CONSISTENCY,
                fieldName.getHeaderName(),
                dumpRow.rowNumber(),
                phs,
                null,
                getFieldValue(dumpRow, fieldName),
                errorMessage
            )
        );
      }
    }
  }

  private boolean compareSets(String csvValue, String dumpValue) {
    if (csvValue == null || dumpValue == null) return false;

    Set<String> csvSet = new HashSet<>(Arrays.asList(csvValue.split(";"))).stream()
        .map(String::trim)
        .map(String::toLowerCase)
        .collect(Collectors.toSet());

    Set<String> dumpSet = new HashSet<>(Arrays.asList(dumpValue.split(","))).stream()
        .map(String::trim)
        .map(String::toLowerCase)
        .collect(Collectors.toSet());

    return csvSet.equals(dumpSet);
  }

  private List<StudyMetadataRow> readCsvToStudyMetadataRows(Path filePath) throws IOException, ParseException {
    List<StudyMetadataRow> metadataRows = new ArrayList<>();

    try (Reader reader = new FileReader(filePath.toFile())) {
      Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(reader);
      int rowNumber = 1;

      for (CSVRecord record : records) {
        metadataRows.add(new StudyMetadataRow(
            rowNumber++,
            record.get("RADx Data Program"),
            record.get("dbGaP Study Accession"),
            record.get("Study Name"),
            null, // description
            null, // radxAcknowledgements
            null, // nihGrantNumber
            null, // studyStartDate
            null, // studyEndDate
            null, // studyReleaseDate
            null, // updatedAt
            null, // foaNumber
            null, // foaUrl
            null, // contactPiProjectLeader
            null, // studyDoi
            null, // publicationUrls
            null, // clinicalTrialsGovUrl
            null, // studyWebsiteUrl
            record.get("Study Design"),
            null, // dataTypes
            record.get("Study Domain"),
            record.get("NIH Institute / Center"),
            null, // multiCenterStudy
            null, // multiCenterSites
            null, // keywords
            record.get("Data Collection Method"),
            Integer.parseInt(record.get("Estimated Cohort Size")),
            record.get("Study Population Focus"),
            null, // species
            null, // consentDataUseLimitations
            null, // studyStatus
            record.get("Has Data Files"),
            null, // diseaseSpecificGroup
            null, // diseaseSpecificRelatedConditions
            null, // healthBiomedGroup
            null, // studyCitation
            null, // actualStudySize
            null, // studyVersion
            null, // estimatedParticipantRange
            null  // createdAt
        ));
      }
    }
    return metadataRows;
  }

  private String getFieldValue(StudyMetadataRow row, SpreadsheetHeaders fieldName) {
    return switch (fieldName) {
      case STUDY_TITLE -> row.studyTitle();
      case ESTIMATED_COHORT_SIZE -> String.valueOf(row.estimatedCohortSize());
      case STUDY_POPULATION_FOCUS -> row.studyPopulationFocus();
      case STUDY_DOMAIN -> row.studyDomain();
      case STUDY_DESIGN -> row.studyDesign();
      case DATA_COLLECTION_METHOD -> row.dataCollectionMethod();
      case NIH_INSTITUTE_OR_CENTER -> row.nihInstituteOrCenter();
      case HAS_DATA_FILES -> row.hasDataFiles();
      default -> "Unknown Field";
    };
  }
}
