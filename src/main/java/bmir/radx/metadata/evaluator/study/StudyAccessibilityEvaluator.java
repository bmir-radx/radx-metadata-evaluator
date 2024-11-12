package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.sharedComponents.LinkChecker;
import bmir.radx.metadata.evaluator.util.TemplateGetter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.sharedComponents.DistributionManager.updateDistribution;
import static bmir.radx.metadata.evaluator.sharedComponents.EvaluationReportUpdater.updateAccessibilityResult;

@Component
public class StudyAccessibilityEvaluator {
  private final LinkChecker linkChecker;
  private final TemplateGetter templateGetter;

  public StudyAccessibilityEvaluator(LinkChecker linkChecker, TemplateGetter templateGetter) {
    this.linkChecker = linkChecker;
    this.templateGetter = templateGetter;
  }

  public void evaluate(List<StudyMetadataRow> rows, Consumer<EvaluationResult> consumer, ValidationSummary<SpreadsheetValidationResult> validationSummary){
    var templateSchemaArtifact = templateGetter.getStudyTemplate();
    var validationResults = validationSummary.getValidationResults();

    Map<Integer, Integer> distributionMap = new HashMap<>();
    Set<String> inaccessibleRecords = new HashSet<>();
    int totalURL = 0;
    int totalResolvableURL = 0;
    for(var row: rows){
      var urlCount = linkChecker.checkSpreadsheet(row, templateSchemaArtifact, validationResults);
      var url = urlCount.getTotalURL();
      totalResolvableURL += urlCount.getResolvableURL();
      totalURL += url;
      updateDistribution(url, distributionMap);

      //update invalid studies
      if(urlCount.getUnresolvableURL() > 0){
        validationSummary.addInvalidMetadata(row.studyPHS());
        inaccessibleRecords.add(row.studyPHS());
      }
    }

    updateAccessibilityResult(totalURL, totalResolvableURL, consumer, distributionMap, inaccessibleRecords);
  }
}
