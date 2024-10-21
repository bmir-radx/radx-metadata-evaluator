package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.util.SpreadsheetUpdater;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import edu.stanford.bmir.radx.metadata.validator.lib.LiteralFieldValidators;
import edu.stanford.bmir.radx.metadata.validator.lib.ValidatorFactory;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.VALIDITY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

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

  public List<SpreadsheetValidationResult> evaluate(Path metadataFilePath, List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer){
    var workbook = getWorkbook(metadataFilePath);
    spreadsheetUpdater.addMetadataTab(workbook, templateTitle, templateVersion, templateCreatedOn, templateID);
    spreadsheetUpdater.patchMetadata(workbook, metadataFilePath);

    var validator = validatorFactory.createValidator(new LiteralFieldValidators(new HashMap<>()));
    List<SpreadsheetValidationResult> validationReports = new ArrayList<>();
    var spreadsheetValidatorResponse = validator.validateSpreadsheet(metadataFilePath.toString());
    var mapping = getRowToPhsMap(rows);

    var invalidStudyRows = new ArrayList<>();
    if(spreadsheetValidatorResponse != null){
      var reports = spreadsheetValidatorResponse.reports();
      reports.forEach(result-> {
          var spreadsheetResult = new SpreadsheetValidationResult(
              result.errorType(),
              result.column(),
              result.row(),
              mapping.get(result.row()),
              result.repairSuggestion(),
              result.value()
          );
          validationReports.add(spreadsheetResult);
          invalidStudyRows.add(result.row());
      });
    }

    int totalStudies = rows.size();
    int invalidStudies = invalidStudyRows.size();
    var rate = (double) (totalStudies - invalidStudies) / totalStudies * 100;
    String formattedRate = String.format("%.2f%%", rate);

    consumer.accept(new EvaluationResult(VALIDITY,VALIDATION_PASS_RATE, formattedRate));
    consumer.accept(new EvaluationResult(VALIDITY, NUMBER_OF_INVALID_STUDIES, String.valueOf(invalidStudies)));
    if(invalidStudies > 0){
      consumer.accept(new EvaluationResult(VALIDITY, INVALID_STUDIES, invalidStudyRows.toString()));
    }
    return validationReports;
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
        report.errorType(),
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
}
