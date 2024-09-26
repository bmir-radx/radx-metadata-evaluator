package bmir.radx.metadata.evaluator.sharedComponents;

import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static bmir.radx.metadata.evaluator.study.FieldNameStandardizer.getStandardizedMap;
import static bmir.radx.metadata.evaluator.study.FieldNameStandardizer.standardizeFieldName;
import static bmir.radx.metadata.evaluator.sharedComponents.FieldRequirement.*;

@Component
public class CompletionRateChecker {
  public <T> CompletionResult checkCompletionRate(T instance, TemplateSchemaArtifact template) {
    Map<FieldRequirement, Integer> counts = new HashMap<>();
    Map<FieldRequirement, Integer> totals = new HashMap<>();

    for (var requirement : FieldRequirement.values()) {
      counts.put(requirement, 0);
      totals.put(requirement, 0);
    }

    var fields = instance.getClass().getDeclaredFields();
    for (var field : fields) {
      field.setAccessible(true);

      String fieldName = field.getName();
      var requirement = getRequirement(fieldName, template);
      if (requirement == null) {
        continue;
      }
      totals.put(requirement, totals.get(requirement) + 1);

      try {
        var value = field.get(instance);
        if (value != null && !value.equals("")) {
          counts.put(requirement, counts.get(requirement) + 1);
        }
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    Map<FieldRequirement, Double> completionRates = new HashMap<>();
    for (FieldRequirement requirement : FieldRequirement.values()) {
      int totalCount = totals.get(requirement);
      int completedCount = counts.get(requirement);
      double completionRate = totalCount > 0 ? (double) completedCount / totalCount : 0.0;
      completionRates.put(requirement, completionRate);
    }

    return new CompletionResult(
        completionRates,
        totals.get(REQUIRED),
        totals.get(RECOMMENDED),
        totals.get(OPTIONAL),
        counts.get(REQUIRED),
        counts.get(RECOMMENDED),
        counts.get(OPTIONAL));
  }

  private FieldRequirement getRequirement(String fieldName, TemplateSchemaArtifact templateSchemaArtifact){
    var templateReporter = new TemplateReporter(templateSchemaArtifact);
    var standardizedMap = getStandardizedMap(templateSchemaArtifact);
    var fieldPath = "/" + standardizedMap.get(standardizeFieldName(fieldName));
    var fieldArtifact = templateReporter.getFieldSchema(fieldPath);
    if(fieldArtifact.isEmpty()){
      return null;
    }
    if(fieldArtifact.get().requiredValue()){
      return REQUIRED;
    } else if (fieldArtifact.get().valueConstraints().get().recommendedValue()) {
      return FieldRequirement.RECOMMENDED;
    } else{
      return FieldRequirement.OPTIONAL;
    }
  }
}
