package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.sharedComponents.LinkChecker;
import bmir.radx.metadata.evaluator.util.ReporterGetter;
import bmir.radx.metadata.evaluator.util.StudyPhsGetter;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.sharedComponents.DistributionManager.updateDistribution;
import static bmir.radx.metadata.evaluator.sharedComponents.EvaluationReportUpdater.updateAccessibilityResult;

@Component
public class DataFileAccessibilityEvaluator {
  private final LinkChecker linkChecker;
  private final ReporterGetter reporterGetter;
  private final StudyPhsGetter studyPhsGetter;

  public DataFileAccessibilityEvaluator(LinkChecker linkChecker, ReporterGetter reporterGetter, StudyPhsGetter studyPhsGetter) {
    this.linkChecker = linkChecker;
    this.reporterGetter = reporterGetter;
    this.studyPhsGetter = studyPhsGetter;
  }

  public void evaluate(Map<Path, TemplateInstanceArtifact> templateInstanceArtifacts, Consumer<EvaluationResult> consumer, ValidationSummary<JsonValidationResult> validationSummary){
    var totalUrlDistribution = new HashMap<Integer, Integer>();
    var totalUrl = 0;
    var resolvableURL = 0;
    var templateReporter = reporterGetter.getTemplateReporter();
    var inaccessibleRecords = new HashSet<String>();
    for(var instance : templateInstanceArtifacts.entrySet()){
      var fileName = instance.getKey().getFileName().toString();
      var instanceArtifact = instance.getValue();
      var studyPHS = studyPhsGetter.getCleanStudyPhs(instanceArtifact);
      var instanceReporter = reporterGetter.getTemplateInstanceValuesReporter(instanceArtifact);
      var urlCount = linkChecker.checkJson(studyPHS, fileName, templateReporter, instanceReporter, validationSummary.getValidationResults());
      //update total url distribution
      updateDistribution(urlCount.getTotalURL(), totalUrlDistribution);
      totalUrl += urlCount.getTotalURL();
      resolvableURL += urlCount.getResolvableURL();
      //update invalid metadata
      if (urlCount.getUnresolvableURL() > 0){
        validationSummary.addInvalidMetadata(fileName);
        inaccessibleRecords.add(fileName);
      }
    }

    updateAccessibilityResult(totalUrl, resolvableURL, consumer, totalUrlDistribution, inaccessibleRecords);
  }
}
