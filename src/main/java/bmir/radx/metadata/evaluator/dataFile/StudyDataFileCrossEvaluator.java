package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.IssueLevel;
import bmir.radx.metadata.evaluator.SpreadsheetHeaders;
import bmir.radx.metadata.evaluator.SpreadsheetReader;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.study.StudyMetadataRow;
import bmir.radx.metadata.evaluator.thirdParty.rePORTER.RePORTERService;
import bmir.radx.metadata.evaluator.util.InstanceArtifactPath;
import bmir.radx.metadata.evaluator.util.InstanceArtifactValueGetter;
import org.jfree.data.json.impl.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.ACCURACY;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.CONSISTENCY;

@Component
public class StudyDataFileCrossEvaluator {
  private static final String DATA_FILE_PARENT_STUDIES = "Data File Parent Studies";
  private static final String STUDY_NAME = "Study Name";
  private static final String PHS_IDENTIFIER = "PHS Identifier";
  private static final String DATA_FILE_FUNDING_SOURCES = "Data File Funding Sources";
  private static final String AWARD_LOCAL_IDENTIFIER = "Award Local Identifier";
  private final SpreadsheetReader spreadsheetReader;
  private final RePORTERService rePORTERService;
  private Map<StudyMetadataRow, Integer> studySampleSizeMap = new HashMap<>();

  public StudyDataFileCrossEvaluator(SpreadsheetReader spreadsheetReader, RePORTERService rePORTERService) {
    this.spreadsheetReader = spreadsheetReader;
    this.rePORTERService = rePORTERService;
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

    // evaluate sample size
    compareSampleSize(validationSummary);
  }

  private void evaluateSinglePair(String dataFileName,
                                  TemplateInstanceArtifact dataFileArtifact,
                                  StudyMetadataRow studyArtifact,
                                  Set<String> inaccurateStudies,
                                  ValidationSummary<JsonValidationResult> validationSummary) {
    var studyPhs = studyArtifact.studyPHS().trim();
    var studyName = studyArtifact.studyTitle();
    var studyGrandNumber = studyArtifact.nihGrantNumber();

    // Check PHS
    var dataFileStudyPhs = InstanceArtifactValueGetter.getStudyPhs(dataFileArtifact);
    if (dataFileStudyPhs != null && !identicalValues(studyPhs, dataFileStudyPhs)) {
        updateResult(studyPhs, dataFileName, studyPhs, dataFileStudyPhs, DATA_FILE_PARENT_STUDIES, PHS_IDENTIFIER, inaccurateStudies, validationSummary, IssueLevel.ERROR);
    }

    // Check Study Name
    var dataFileStudyName = InstanceArtifactValueGetter.getStudyName(dataFileArtifact);
    if (dataFileStudyName != null && !identicalValues(studyName, dataFileStudyName)) {
      updateResult(studyPhs, dataFileName, studyName, dataFileStudyName, DATA_FILE_PARENT_STUDIES, STUDY_NAME, inaccurateStudies, validationSummary, IssueLevel.ERROR);
    }

    // Check Award Local Identifier
    var dataFileFundingId = InstanceArtifactValueGetter.getAwardLocalIdentifier(dataFileArtifact);
    if (dataFileFundingId != null) {
      var dataFileFundingIdString = dataFileFundingId.replaceAll(" ", "");
      if(!identicalValues(studyGrandNumber, dataFileFundingIdString)){
        //Call rePORTER to check if the data file funding id exist
        var results = rePORTERService.sendPostRequest(dataFileFundingIdString).results();
        var issueLevel = IssueLevel.REVIEW_NEEDED;
        if(results.isEmpty()){
          issueLevel = IssueLevel.ERROR;
        }
        updateResult(studyPhs, dataFileName, studyGrandNumber, dataFileFundingId, DATA_FILE_FUNDING_SOURCES, AWARD_LOCAL_IDENTIFIER, inaccurateStudies, validationSummary, issueLevel);
      }
    }

    // Check estimated sample size
//    var row = studyArtifact.rowNumber();
//    var dataCharSummary = InstanceArtifactValueGetter.getDataCharSummary(dataFileArtifact);
//    var studySize = studyArtifact.estimatedCohortSize();
//    int dataSummarySize = getSampleSizeFromHTML(dataCharSummary);
//    if(! sameSampleSize(studySize, dataSummarySize)){
//      updateResult4SampleSize(row, studyPhs, dataFileName, studySize, dataSummarySize, inaccurateStudies, validationSummary);
//    }

    // Update sample size map
    var dataCharSummary = InstanceArtifactValueGetter.getDataCharSummary(dataFileArtifact);
    int dataSummarySize = getSampleSizeFromHTML(dataCharSummary);
    studySampleSizeMap.merge(studyArtifact, dataSummarySize, Math::max);
  }

  private boolean identicalValues(String studyMetadata, String dataFileMetadata){
    Set<String> normalizedStudyMetadataSet = Arrays.stream(studyMetadata.split(","))
        .map(String::trim)
        .map(this::normalizeMetadata)
        .collect(Collectors.toSet());
    var normalizedDataFileMetadata = normalizeMetadata(dataFileMetadata);
    return normalizedStudyMetadataSet.contains(normalizedDataFileMetadata);
  }

  private void updateResult(String studyPhs,
                            String dataFileName,
                            String studyMetadata,
                            String dataFileMetadata,
                            String element,
                            String field,
                            Set<String> inaccurateStudies,
                            ValidationSummary<JsonValidationResult> validationSummary,
                            IssueLevel issueLevel){
    inaccurateStudies.add(dataFileName);
    var pointer = getPointer(element, field);
    var errorMessage = getErrorMessage(field, dataFileMetadata, studyMetadata);
    validationSummary.addInvalidMetadata(dataFileName);
    validationSummary.updateValidationResult(
        new JsonValidationResult(
            studyPhs,
            dataFileName,
            pointer,
            CONSISTENCY,
            errorMessage,
            studyMetadata,
            issueLevel,
            dataFileMetadata
        )
    );
  }

  private String normalizeMetadata(String sentence) {
    // Remove punctuation, extra spaces, normalize casing, and trim
    return Normalizer.normalize(sentence, Normalizer.Form.NFD)
        .replaceAll("[^a-zA-Z0-9\\s]", "") // Remove non-alphanumeric characters except spaces
        .toLowerCase()
        .replaceAll("\\s+", " ")
        .trim();
  }

  private String getPointer(String element, String field){
    return element +
        "[0]/" +
        field;
  }

  private String getErrorMessage(String field, String value, String studyMetadata){
    return field + " [" + value + "] does not match the study metadata [" + studyMetadata + "]";
  }

  private Integer getSampleSizeFromHTML(String html){
    if(html==null || html.equals("")){
      return null;
    }
    Document doc = Jsoup.parse(html);
    // A regex pattern to match the "n / N (%)" format
    Pattern pattern = Pattern.compile("(\\d+)\\s*/\\s*(\\d+)\\s*\\(([^%)]+)%\\)");
    Elements elements = doc.select("td, p");
    int maxN = Integer.MIN_VALUE;

    // 4) Search for the pattern in each element’s text
    for (Element el : elements) {
      String text = el.text().trim();
      Matcher matcher = pattern.matcher(text);
      while (matcher.find()) {
        // Group(1) = n
        // Group(2) = N
        // Group(3) = the numeric part of the percentage
        String NStr = matcher.group(2);
        try {
          int NValue = Integer.parseInt(NStr);
          if (NValue > maxN) {
            maxN = NValue;
          }

        } catch (NumberFormatException e) {
        }
      }
    }

    return maxN;
  }

  private void compareSampleSize(ValidationSummary<JsonValidationResult> validationSummary){
    for(var sampleSizeEntry: studySampleSizeMap.entrySet()){
      var studyMetadataRow = sampleSizeEntry.getKey();
      var sampleSizeFromStudy = studyMetadataRow.estimatedCohortSize();
      var maxSizeFromDataFile = sampleSizeEntry.getValue();
      if(!sameSampleSize(sampleSizeFromStudy, maxSizeFromDataFile)){
        updateResult4SampleSize(
            studyMetadataRow.rowNumber(),
            studyMetadataRow.studyPHS(),
            sampleSizeFromStudy,
            maxSizeFromDataFile,
            validationSummary);
      }
    }
  }

  private boolean sameSampleSize(int studySize, Integer dataFileSize) {
    if (dataFileSize == null) {
      return true;
    }

    double lowerBound = dataFileSize * 0.8;
    double upperBound = dataFileSize * 1.2;

    return studySize >= lowerBound && studySize <= upperBound;
  }

  /*
  Add a spreadsheet-result type result, this should be a study metadata issue
  Put the row number in the suggestion.
   */
  private void updateResult4SampleSize(int row,
                                       String studyPhs,
                                       int studySize,
                                       Integer dataFileMetadata,
                                       ValidationSummary<JsonValidationResult> validationSummary){
    var errorMessage = String.format("The estimated cohort size in the study metadata [%s] is inconsistent with the Data Characteristics Table in the data file metadata [%s]", dataFileMetadata, studySize);
    validationSummary.updateValidationResult(
        new JsonValidationResult(
            studyPhs,
            null,
            SpreadsheetHeaders.ESTIMATED_COHORT_SIZE.getHeaderName(),
            CONSISTENCY,
            errorMessage,
            String.valueOf(row), //suggestion
            IssueLevel.REVIEW_NEEDED,
            String.valueOf(studySize)
        )
    );
  }
}
