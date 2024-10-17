package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.EvaluationConstant;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.CONSISTENT_STUDY_RATE;

@Component
public class StudyConsistencyEvaluator {
  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, List<SpreadsheetValidationResult> validationResults){
    List<Integer> inconsistentMultiSitesRows = new ArrayList<>();
    for(var row: rows){
      if (!multiCenterConsistent(row)){
        inconsistentMultiSitesRows.add(row.rowNumber());
        validationResults.add(
            new SpreadsheetValidationResult("Inconsistent Multi-Center Study",
                "MULTI-CENTER STUDY?",
                row.rowNumber(),
                row.studyPHS(),
                "",
                "MULTI-CENTER STUDY?" + row.multiCenterStudy() + "; MULTI-CENTER SITES:" + row.multiCenterSites())
        );
      }
    }
    var totalStudies = rows.size();
    var rate = (double) (totalStudies - inconsistentMultiSitesRows.size()) / totalStudies * 100;
    String formattedRate = String.format("%.2f%%", rate);
    consumer.accept(new EvaluationResult(CONSISTENT_STUDY_RATE, formattedRate));
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
}
