package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.sharedComponents.LinkChecker;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.URL_COUNT_DISTRIBUTION;

@Component
public class StudyLinkEvaluator {
  private final LinkChecker linkChecker;
  private final StudyTemplateGetter studyTemplateGetter;

  public StudyLinkEvaluator(LinkChecker linkChecker, StudyTemplateGetter studyTemplateGetter) {
    this.linkChecker = linkChecker;
    this.studyTemplateGetter = studyTemplateGetter;
  }

  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, List<SpreadsheetValidationResult> validationResults){
    var templateSchemaArtifact = studyTemplateGetter.getTemplate();

    Map<Integer, Integer> distributionMap = new HashMap<>();
    for(var row: rows){
      var urlCount = linkChecker.evaluate(row, templateSchemaArtifact, validationResults);
      var totalURL = urlCount.getTotalURL();
      distributionMap.put(
          totalURL,
          distributionMap.getOrDefault(totalURL, 0) + 1);
    }

    consumer.accept(new EvaluationResult(URL_COUNT_DISTRIBUTION, distributionMap.toString()));
  }
}
