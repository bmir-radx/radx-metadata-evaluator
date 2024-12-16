package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.thirdParty.OllamaResponse;
import bmir.radx.metadata.evaluator.thirdParty.OllamaService;
import bmir.radx.metadata.evaluator.thirdParty.clinicalTrials.ClinicalTrialsResponse;
import bmir.radx.metadata.evaluator.thirdParty.clinicalTrials.ClinicalTrialsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static bmir.radx.metadata.evaluator.HeaderName.CLINICALTRIALS_GOV_URL;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.ACCESSIBILITY;

@Component
public class ClinicalTrialsChecker {
  private final ClinicalTrialsService clinicalTrialsService;
  private final OllamaService ollamaService;

  public ClinicalTrialsChecker(ClinicalTrialsService clinicalTrialsService, OllamaService ollamaService) {
    this.clinicalTrialsService = clinicalTrialsService;
    this.ollamaService = ollamaService;
  }

  public List<Integer> checkInvalidClinicalTrialsLink(List<StudyMetadataRow> metadataRows,
                                             List<SpreadsheetValidationResult> validationResults) {
    List<Integer> incorrectCtLink = new ArrayList<>();
    for (StudyMetadataRow row : metadataRows) {
      String clinicalTrialsGovUrl = row.clinicalTrialsGovUrl();
      String nctId = extractNctId(clinicalTrialsGovUrl);

      if (nctId == null) {
//        addValidationResult(validationResults, row.rowNumber(), clinicalTrialsGovUrl);
        continue;
      }

      // Handle valid NCT ID without query parameters
      if (!nctId.contains("?")) {
        var valid = checkValidNctId(row, nctId, validationResults);
        if(!valid){
          incorrectCtLink.add(row.rowNumber());
        }
      } else {
        // Handle NCT ID with query parameters
        addValidationResult(validationResults, row.rowNumber(), row.studyPHS(), clinicalTrialsGovUrl);
        incorrectCtLink.add(row.rowNumber());
      }
    }
    return incorrectCtLink;
  }

  private boolean checkValidNctId(StudyMetadataRow row, String nctId,
                                  List<SpreadsheetValidationResult> validationResults) {
    var response = clinicalTrialsService.sendGetRequest(nctId);

    if (response != null) {
      OllamaResponse llmResponse = processClinicalTrialResponse(row, response);
      if (llmResponse != null && !isSameStudy(llmResponse)) {
        addValidationResult(validationResults, row.rowNumber(), row.studyPHS(), row.clinicalTrialsGovUrl());
        return false;
      }
    } else {
      addValidationResult(validationResults, row.rowNumber(), row.studyPHS(), row.clinicalTrialsGovUrl());
      return false;
    }
    return true;
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
        ACCESSIBILITY,
        CLINICALTRIALS_GOV_URL.getHeaderName(),
        rowNumber,
        phs,
        null,
        link,
        "Invalid URL"
    );
    validationResults.add(validationResult);
  }
}
