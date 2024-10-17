package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.thirdParty.OllamaResponse;
import bmir.radx.metadata.evaluator.thirdParty.OllamaService;
import bmir.radx.metadata.evaluator.thirdParty.clinicalTrials.ClinicalTrialsResponse;
import bmir.radx.metadata.evaluator.thirdParty.clinicalTrials.ClinicalTrialsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Component
public class ClinicalTrialsChecker {
  private final ClinicalTrialsService clinicalTrialsService;
  private final OllamaService ollamaService;

  public ClinicalTrialsChecker(ClinicalTrialsService clinicalTrialsService, OllamaService ollamaService) {
    this.clinicalTrialsService = clinicalTrialsService;
    this.ollamaService = ollamaService;
  }

  public void checkClinicalTrialsContent(List<StudyMetadataRow> metadataRows,
                                         Consumer<EvaluationResult> consumer,
                                         List<SpreadsheetValidationResult> validationResults) {
    for (StudyMetadataRow row : metadataRows) {
      String clinicalTrialsGovUrl = row.clinicalTrialsGovUrl();
      String nctId = extractNctId(clinicalTrialsGovUrl);

      if (nctId == null) {
//        addValidationResult(validationResults, row.rowNumber(), clinicalTrialsGovUrl);
        continue;
      }

      // Handle valid NCT ID without query parameters
      if (!nctId.contains("?")) {
        handleValidNctId(row, nctId, validationResults);
      } else {
        // Handle NCT ID with query parameters
        addValidationResult(validationResults, row.rowNumber(), row.studyPHS(), clinicalTrialsGovUrl);
      }
    }
  }

  private void handleValidNctId(StudyMetadataRow row, String nctId,
                                List<SpreadsheetValidationResult> validationResults) {
    var response = clinicalTrialsService.sendGetRequest(nctId);

    if (response != null) {
      OllamaResponse llmResponse = processClinicalTrialResponse(row, response);
      if (llmResponse != null && !isSameStudy(llmResponse)) {
        addValidationResult(validationResults, row.rowNumber(), row.studyPHS(), row.clinicalTrialsGovUrl());
      }
    } else {
      addValidationResult(validationResults, row.rowNumber(), row.studyPHS(), row.clinicalTrialsGovUrl());
    }
  }

  private OllamaResponse processClinicalTrialResponse(StudyMetadataRow row, ClinicalTrialsResponse response) {
    String cTitle = response.protocolSection().identificationModule().officialTitle();
    String briefDes = response.protocolSection().descriptionModule().briefSummary();
    String detailedDes = response.protocolSection().descriptionModule().detailedDescription();

    String title = row.studyTitle();
    String description = row.description();

    return ollamaService.sendRequest(
        escapeJsonString(title),
        escapeJsonString(cTitle),
        escapeJsonString(description),
        escapeJsonString(briefDes + " " + detailedDes)
    );
  }


  private String extractNctId(String clinicalTrialsGovUrl) {
    if (clinicalTrialsGovUrl != null && clinicalTrialsGovUrl.contains("/NCT")) {
      return clinicalTrialsGovUrl.split(";")[0].substring(clinicalTrialsGovUrl.lastIndexOf("/") + 1);
    }
    return null;
  }

  private String escapeJsonString(String input) {
    if (input == null) {
      return null;
    }
    ObjectMapper mapper = new ObjectMapper();
    try {
      String escaped = mapper.writeValueAsString(input);
      return escaped.substring(1, escaped.length() - 1);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("Error escaping string", e);
    }
  }

  private boolean isSameStudy(OllamaResponse response){
    if (response==null){
      return false;
    }
    return response.message().content().contains("Yes");
  }

  private void addValidationResult(List<SpreadsheetValidationResult> validationResults,
                                   int rowNumber,
                                   String phs,
                                   String link){
    var validationResult = new SpreadsheetValidationResult(
        "Wrong clinicalTrials.gov link",
        "CLINICALTRIALS.GOV URL",
        rowNumber,
        phs,
        null,
        link
    );
    validationResults.add(validationResult);
  }
}
