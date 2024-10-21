package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.sharedComponents.LinkChecker;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.ACCESSIBILITY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.RESOLVABLE_URL_RATE;
import static bmir.radx.metadata.evaluator.EvaluationMetric.URL_COUNT_DISTRIBUTION;

@Component
public class StudyAccessibilityEvaluator {
  private final LinkChecker linkChecker;
  private final StudyTemplateGetter studyTemplateGetter;

  public StudyAccessibilityEvaluator(LinkChecker linkChecker, StudyTemplateGetter studyTemplateGetter) {
    this.linkChecker = linkChecker;
    this.studyTemplateGetter = studyTemplateGetter;
  }

  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, List<SpreadsheetValidationResult> validationResults){
    var templateSchemaArtifact = studyTemplateGetter.getTemplate();

    Map<Integer, Integer> distributionMap = new HashMap<>();
    int totalURL = 0;
    int totalResolvableURL = 0;
    for(var row: rows){
      var urlCount = linkChecker.evaluate(row, templateSchemaArtifact, validationResults);
      var url = urlCount.getTotalURL();
      totalResolvableURL += urlCount.getResolvableURL();
      totalURL += url;
      distributionMap.put(
          url,
          distributionMap.getOrDefault(url, 0) + 1);
    }

    var rate = (double) totalResolvableURL / totalURL * 100;
    String formattedRate = String.format("%.2f%%", rate);
    consumer.accept(new EvaluationResult(ACCESSIBILITY, RESOLVABLE_URL_RATE, formattedRate));
    consumer.accept(new EvaluationResult(ACCESSIBILITY, URL_COUNT_DISTRIBUTION, distributionMap.toString()));
  }
}
