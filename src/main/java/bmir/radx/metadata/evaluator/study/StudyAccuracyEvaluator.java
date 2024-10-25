package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.ACCURACY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

@Component
public class StudyAccuracyEvaluator {
  private final ClinicalTrialsChecker clinicalTrialsChecker;

  public StudyAccuracyEvaluator(ClinicalTrialsChecker clinicalTrialsChecker) {
    this.clinicalTrialsChecker = clinicalTrialsChecker;
  }

  public void evaluate(List<StudyMetadataRow> metadataRows,
                       Consumer<EvaluationResult> consumer,
                       ValidationSummary<SpreadsheetValidationResult> validationSummary){
    var validationResults = validationSummary.getValidationResults();
    var incorrectCtLinks = clinicalTrialsChecker.checkInvalidClinicalTrialsLink(metadataRows, validationResults);
    int totalStudies = metadataRows.size();
    int inAccurateStudies = incorrectCtLinks.size();
    var rate = (double) (totalStudies - inAccurateStudies) / totalStudies * 100;
    String formattedRate = String.format("%.2f%%", rate);
    consumer.accept(new EvaluationResult(ACCURACY, ESTIMATED_ACCURATE_STUDY_RATE, formattedRate));
    consumer.accept(new EvaluationResult(ACCURACY, NUMBER_OF_ESTIMATED_INACCURATE_STUDIES, String.valueOf(inAccurateStudies)));
    if(inAccurateStudies > 0){
      consumer.accept(new EvaluationResult(ACCURACY, ESTIMATED_INACCURATE_STUDIES, incorrectCtLinks.toString()));
    }
  }
}
