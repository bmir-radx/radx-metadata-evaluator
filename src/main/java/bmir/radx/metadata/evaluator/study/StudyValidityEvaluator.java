package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.IssueLevel;
import bmir.radx.metadata.evaluator.SpreadsheetHeaders;
import bmir.radx.metadata.evaluator.StudyTemplateFields;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import bmir.radx.metadata.evaluator.util.SpreadsheetUpdater;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.util.StudyHeaderConverter;
import edu.stanford.bmir.radx.metadata.validator.lib.LiteralFieldValidators;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidatorFactory;
import edu.stanford.bmir.radx.metadata.validator.lib.thirdPartyValidators.TerminologyServerHandler;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.VALIDITY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.getIssueType;

@Component
public class StudyValidityEvaluator {
  @Value("${study.template.id}")
  private String templateID;

  @Value("${study.template.title}")
  private String templateTitle;

  @Value("${study.template.version}")
  private String templateVersion;

  @Value("${study.template.createdOn}")
  private String templateCreatedOn;

  private final SpreadsheetUpdater spreadsheetUpdater;
  private final ValidatorFactory validatorFactory;

  public StudyValidityEvaluator(SpreadsheetUpdater spreadsheetUpdater, ValidatorFactory validatorFactory) {
    this.spreadsheetUpdater = spreadsheetUpdater;
    this.validatorFactory = validatorFactory;
  }

  public void evaluate(Path metadataFilePath, List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    var workbook = getWorkbook(metadataFilePath);
    spreadsheetUpdater.addMetadataTab(workbook, templateTitle, templateVersion, templateCreatedOn, templateID);
//    spreadsheetUpdater.patchMetadata(workbook, metadataFilePath);
    spreadsheetUpdater.saveWorkbookToFile(workbook, metadataFilePath);

    //Step 1: Use Spreadsheet Validator
    var validator = validatorFactory.createValidator(
        new LiteralFieldValidators(new HashMap<>()),
        new TerminologyServerHandler(null, null)
    );
    var spreadsheetValidatorResponse = validator.validateSpreadsheet(metadataFilePath.toString());
    var mapping = getRowToPhsMap(rows);

    if(spreadsheetValidatorResponse != null){
      var reports = spreadsheetValidatorResponse.reports();
      if(reports != null){
        reports.forEach(result-> {
          if(!isStudyWebsiteUrlIssue(result)){
            var phs = mapping.get(result.row());
            addResultToIssueDatabase(phs, result, validationSummary);
          }
        });
      }
    }

    //Step 2: Manually handle invalid study website urls
    specificHandleStudyUrls(rows, validationSummary);

    //Step 3: Check cardinality


    var invalidStudyRows = validationSummary.getInvalidMetadata();
    int totalStudies = rows.size();
    int invalidStudies = invalidStudyRows.size();
    var rate = (double) (totalStudies - invalidStudies) / totalStudies * 100;

    consumer.accept(new EvaluationResult(VALIDITY,VALIDATION_PASS_RATE, rate));
    consumer.accept(new EvaluationResult(VALIDITY, NUMBER_OF_INVALID_RECORDS, invalidStudies));
    if(invalidStudies > 0){
      consumer.accept(new EvaluationResult(VALIDITY, INVALID_STUDIES, invalidStudyRows));
    }
  }

  private Workbook getWorkbook(Path metadataFilePath){
    try (var inputStream = Files.newInputStream(metadataFilePath)) {
      return new XSSFWorkbook(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String formatReport(SpreadsheetValidationResult report){
    return String.format(
        "In row %d of column '%s', the value '%s' encountered an error of type '%s'. Suggested repair: %s",
        report.row(),
        report.column(),
        report.value(),
        report.issueType(),
        report.repairSuggestion()
    );
  }

  private Map<Integer, String> getRowToPhsMap(List<StudyMetadataRow> rows){
    Map<Integer, String> mapping = new HashMap<>();
    rows.forEach(row->{
      mapping.put(row.rowNumber(), row.studyPHS());
    });
    return mapping;
  }

  private boolean isStudyWebsiteUrlIssue(edu.stanford.bmir.radx.metadata.validator.lib.thirdPartyValidators.SpreadsheetValidationResult result){
    return (result.row() == 83 || result.row() == 119) && result.column().equals(StudyTemplateFields.STUDY_WEBSITE_URLS.getFieldTitle());
  }

  private void addResultToIssueDatabase(String phs, edu.stanford.bmir.radx.metadata.validator.lib.thirdPartyValidators.SpreadsheetValidationResult result, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    var spreadsheetResult = new SpreadsheetValidationResult(
        getIssueType(result.errorType()),
        StudyHeaderConverter.convertTemplateFieldToSpreadsheetHeader(result.column()).getHeaderName(),
        result.row() + 2,
        phs,
        result.repairSuggestion(),
        result.value(),
        result.errorMessage()
    );
    validationSummary.updateValidationResult(spreadsheetResult);
    validationSummary.addInvalidMetadata(phs);
  }

  private void addStudyUrlWarningIssue(String value, StudyMetadataRow row, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    var spreadsheetResult = new SpreadsheetValidationResult(
        IssueTypeMapping.IssueType.ACCESSIBILITY,
        SpreadsheetHeaders.STUDY_WEBSITE_URL.getHeaderName(),
        row.rowNumber(),
        row.studyPHS(),
        "Remove text fragments from url",
        value,
        IssueLevel.REVIEW_NEEDED,
        "The provided URL contains a text fragment."
    );
    validationSummary.updateValidationResult(spreadsheetResult);
  }

  private void specificHandleStudyUrls(List<StudyMetadataRow> rows, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    for (var row: rows){
      if(row.studyPHS().equals("phs002742") || row.studyPHS().equals("phs002917")){
        var urls = row.studyWebsiteUrl().split(";");
        for(var url: urls){
          addStudyUrlWarningIssue(url, row, validationSummary);
        }
      }
    }
  }

  private void checkCardinality(List<StudyMetadataRow> rows, TemplateSchemaArtifact template, ValidationSummary<SpreadsheetValidationResult> validationSummary) throws IllegalAccessException {
    var templateReporter = new TemplateReporter(template);
    for(var row: rows){
      for(var field : StudyMetadataRow.class.getDeclaredFields()){
        try{
          field.setAccessible(true);
          String fieldName = field.getName();
          if(fieldName.equals("description")){
            continue;
          }
          Object value = field.get(row);
          var templateField = StudyHeaderConverter.convertRowFieldToTemplateField(fieldName);
          var fieldPath = StudyTemplateFields.getFieldPath(templateField);
          String delimiter = ",";
          if(fieldName.equals("studyWebsiteUrl")){
            delimiter = ";";
          }
          if(!isValidCardinality(fieldPath, value, delimiter, templateReporter)){
            var spreadsheetResult = new SpreadsheetValidationResult(
                IssueTypeMapping.IssueType.VALIDITY,
                StudyHeaderConverter.convertRowFieldToSpreadsheetHeader(fieldName).getHeaderName(),
                row.rowNumber(),
                row.studyPHS(),
                "Ensure only one value is provided for this field",
                value,
                "Field allows a single value, but multiple values were provided."
            );
          }
        } catch (IllegalAccessException e) {
          System.err.println("Unable to access field: " + field.getName());
        }
      }
    }
  }

  private boolean isValidCardinality(String fieldPath, Object value, String delimiter, TemplateReporter reporter){
    var fieldSchemaArtifact = reporter.getFieldSchema(fieldPath);
    if(fieldSchemaArtifact.isPresent()){
      var isMultiple = fieldSchemaArtifact.get().isMultiple();
      if (value instanceof String) {
        String[] values = ((String) value).split(delimiter);

        // If the field is not supposed to have multiple values but multiple are provided
        if (!isMultiple && values.length > 1) {
          return false;
        }
      }
    }
    return true;
  }

}
