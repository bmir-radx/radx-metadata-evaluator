package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.util.SpreadsheetUpdater;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.thirdParty.SpreadsheetValidatorService;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.ERRORS_NUMBER;

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
  private final SpreadsheetValidatorService service;

  public StudyValidityEvaluator(SpreadsheetUpdater spreadsheetUpdater, SpreadsheetValidatorService service) {
    this.spreadsheetUpdater = spreadsheetUpdater;
    this.service = service;
  }

  public List<SpreadsheetValidationResult> evaluate(Path metadataFilePath, Consumer<EvaluationResult> consumer){
    var workbook = getWorkbook(metadataFilePath);
    spreadsheetUpdater.addMetadataTab(workbook, templateTitle, templateVersion, templateCreatedOn, templateID);
    spreadsheetUpdater.patchMetadata(workbook, metadataFilePath);
    List<SpreadsheetValidationResult> validationReports = new ArrayList<>();
    var spreadsheetValidatorResponse = service.validateSpreadsheet(metadataFilePath.toString());
    if(spreadsheetValidatorResponse != null){
      validationReports = spreadsheetValidatorResponse.reports();
    }
    consumer.accept(new EvaluationResult(ERRORS_NUMBER, String.valueOf(validationReports.size())));
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
}
