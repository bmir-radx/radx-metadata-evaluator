package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.IssueLevel;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.sharedComponents.LinguisticQualityChecker;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;
import bmir.radx.metadata.evaluator.util.StudyPhsGetter;
import edu.stanford.bmir.radx.metadata.validator.lib.FieldValues;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.FieldInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class DataFileLingQualityEvaluator {
  private final String HTML_FIELD_NAME = "Data Characteristics Table in HTML";
  private final StudyPhsGetter studyPhsGetter;

  public DataFileLingQualityEvaluator(StudyPhsGetter studyPhsGetter) {
    this.studyPhsGetter = studyPhsGetter;
  }


  public void evaluate(Map<Path, TemplateInstanceArtifact> templateInstanceArtifacts,
                       Consumer<EvaluationResult> consumer,
                       ValidationSummary<JsonValidationResult> validationSummary){
    for(var instanceEntry: templateInstanceArtifacts.entrySet()){
      var path = instanceEntry.getKey();
      var instance = instanceEntry.getValue();
      var phs = studyPhsGetter.getCleanStudyPhs(instance);
      var valueReporter = new TemplateInstanceValuesReporter(instance);
      var values = valueReporter.getValues();
      for(var value: values.entrySet()){
        var fieldPath = value.getKey();
        if(fieldPath.contains(HTML_FIELD_NAME)){
          continue;
        }
        var fieldValues = value.getValue();
        var jsonLdValue = fieldValues.jsonLdValue().orElse(null);
        var jsonLdId = fieldValues.jsonLdId().orElse(null);

        if(jsonLdValue != null){
          checkString(jsonLdValue, phs, fieldPath, path, fieldValues, validationSummary);
        }

        if(jsonLdId != null){
          checkString(jsonLdId.toString(), phs, fieldPath, path, fieldValues, validationSummary);
        }
      }
    }
  }

  private void checkString(String value, String phs, String fieldLoc, Path filePath, FieldValues fieldValues, ValidationSummary<JsonValidationResult> validationSummary){
    List<Integer> index = LinguisticQualityChecker.checkExtraSpace(value);
    if (!index.isEmpty()) {
      validationSummary.updateValidationResult(
          new JsonValidationResult(
              phs,
              filePath.getFileName().toString(),
              fieldLoc.substring(1),
              IssueTypeMapping.IssueType.LINGUISTIC_QUALITY,
              "Extra space(s) found",
              "Remove extra space at index " + index.toString(),
//              IssueLevel.REVIEW_NEEDED,
              value
          )
      );
    }
  }
}
