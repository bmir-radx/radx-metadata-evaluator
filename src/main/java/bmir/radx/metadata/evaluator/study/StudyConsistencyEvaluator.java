package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.CONSISTENCY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.HeaderName.MULTI_CENTER_STUDY;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.INCONSISTENCY;

@Component
public class StudyConsistencyEvaluator {
  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    List<String> inconsistentMultiSitesRows = new ArrayList<>();
    var validationResults = validationSummary.getValidationResults();
    for(var row: rows){
      if (!multiCenterConsistent(row)){
        inconsistentMultiSitesRows.add(row.studyPHS());
        validationSummary.addInvalidMetadata(row.studyPHS());
        validationResults.add(
            new SpreadsheetValidationResult(INCONSISTENCY,
                MULTI_CENTER_STUDY.getHeaderName(),
                row.rowNumber(),
                row.studyPHS(),
                getSuggestion(row.multiCenterSites()),
                row.multiCenterStudy(),
                "Multiple study sites are provided"
                )
        );
      }
    }
    int totalStudies = rows.size();
    int inconsistentStudies = inconsistentMultiSitesRows.size();
    var rate = (double) (totalStudies - inconsistentStudies) / totalStudies * 100;
    consumer.accept(new EvaluationResult(CONSISTENCY, CONSISTENT_RECORD_RATE, rate));
    consumer.accept(new EvaluationResult(CONSISTENCY, NUMBER_OF_INCONSISTENT_RECORDS, inconsistentStudies));
    if(inconsistentStudies > 0){
      consumer.accept(new EvaluationResult(CONSISTENCY, INCONSISTENT_STUDIES, inconsistentMultiSitesRows));
    }
  }

  private boolean multiCenterConsistent(StudyMetadataRow row){
    var isMultiCenterStudy = row.multiCenterStudy();
    return isMultiCenterStudy == isMultiSitesString(row.multiCenterSites());
  }

  private Boolean isMultiSitesString(String sites){
    if(sites == null){
      return false;
    }
    var lowerCaseSites = sites.toLowerCase();
    return lowerCaseSites.contains("and") || lowerCaseSites.contains(";");
  }

  private String getSuggestion(String sites){
    if(isMultiSitesString(sites)){
      return "Yes";
    } else{
      return "No";
    }
  }
}
