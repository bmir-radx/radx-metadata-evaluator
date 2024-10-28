package bmir.radx.metadata.evaluator.sharedComponents;

import bmir.radx.metadata.evaluator.dataFile.FieldsCollector;
import bmir.radx.metadata.evaluator.dataFile.RecommendedFields;
import bmir.radx.metadata.evaluator.util.FieldRequirement;
import edu.stanford.bmir.radx.metadata.validator.lib.AttributeValueFieldValues;
import edu.stanford.bmir.radx.metadata.validator.lib.FieldValues;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.*;

import static bmir.radx.metadata.evaluator.sharedComponents.DistributionManager.updateDistribution;
import static bmir.radx.metadata.evaluator.study.FieldNameStandardizer.getStandardizedMap;
import static bmir.radx.metadata.evaluator.study.FieldNameStandardizer.standardizeFieldName;
import static bmir.radx.metadata.evaluator.util.FieldRequirement.*;

@Component
public class CompletionRateChecker {
  private final FieldsCollector fieldsCollector;

  public CompletionRateChecker(FieldsCollector fieldsCollector) {
    this.fieldsCollector = fieldsCollector;
  }


  public Map<FieldRequirement, Map<Integer, Integer>> initializeCompletenessDistribution() {
    Map<FieldRequirement, Map<Integer, Integer>> completenessDistribution = new HashMap<>();
    for (var requirement : FieldRequirement.values()) {
      completenessDistribution.put(requirement, new HashMap<>());
    }
    return completenessDistribution;
  }

  public <T> CompletionResult getSpreadsheetRowCompleteness(T instance, TemplateSchemaArtifact template) {
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
      totals.put(OVERALL, totals.get(requirement) + 1);

      try {
        var value = field.get(instance);
        if (value != null && !value.equals("")) {
          counts.put(requirement, counts.get(requirement) + 1);
          counts.put(OVERALL, counts.get(requirement) + 1);
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
        totals.get(OVERALL),
        counts.get(REQUIRED),
        counts.get(RECOMMENDED),
        counts.get(OPTIONAL),
        counts.get(OVERALL));
  }

  public void updateCompletenessDistribution(CompletionResult result, Map<FieldRequirement, Map<Integer, Integer>> completenessDistribution){
    Map<FieldRequirement, Integer> filledFieldsMap = new HashMap<>();
    filledFieldsMap.put(REQUIRED, result.filledRequiredFields());
    filledFieldsMap.put(RECOMMENDED, result.filledRecommendedFields());
    filledFieldsMap.put(OPTIONAL, result.filledOptionalFields());
    filledFieldsMap.put(OVERALL, result.totalFilledFields());

    for (var requirement : FieldRequirement.values()) {
      int filledFields = filledFieldsMap.get(requirement);
      Map<Integer, Integer> distributionMap = completenessDistribution.get(requirement);
      updateDistribution(filledFields, distributionMap);
    }
  }

  public CompletionResult getSingleDataFileCompleteness(TemplateSchemaArtifact templateSchemaArtifact, TemplateInstanceValuesReporter templateInstanceValuesReporter){
    var templateReporter = new TemplateReporter(templateSchemaArtifact);
    var values = templateInstanceValuesReporter.getValues();
    var attributeValueFields = templateInstanceValuesReporter.getAttributeValueFields();
    var allFields = fieldsCollector.getAllFields(templateSchemaArtifact);

    HashSet<String> filledRequiredFields = new HashSet<>();
    HashSet<String> filledRecommendedFields = new HashSet<>();
    HashSet<String> filledOptionalFields = new HashSet<>();
    HashSet<String> filledElements = new HashSet<>();
    HashSet<String> checkedFields = new HashSet<>();

    int requiredFieldCount = 0;
    int recommendedFieldCount = 0;
    int optionalFieldCount = 0;

    for(var field:allFields){
      var fieldConstraint = templateReporter.getValueConstraints(field);
      if (isRequiredField(fieldConstraint)) {
        requiredFieldCount++;
      } else if (isRecommendedField(field)) {
        recommendedFieldCount++;
      } else{
        optionalFieldCount++;
      }
    }
    //update optional filed count with attribute value fields
    optionalFieldCount = optionalFieldCount + attributeValueFields.size();
    int totalFieldCount = requiredFieldCount + recommendedFieldCount + optionalFieldCount;

    int elementCount = templateSchemaArtifact.getElementKeys().size();

    for (Map.Entry<String, FieldValues> fieldEntry : values.entrySet()) {
      var path = fieldEntry.getKey();
      var normalizedPath = path.replaceAll("\\[\\d+\\]", "");
      var value = fieldEntry.getValue();
      var fieldConstraint = templateReporter.getValueConstraints(normalizedPath);
      if(!checkedFields.contains(normalizedPath) && !fieldsCollector.isEmptyField(value)){
        if (isRequiredField(fieldConstraint)) {
          filledRequiredFields.add(normalizedPath);
        } else if (isRecommendedField(normalizedPath) ) {
          filledRecommendedFields.add(normalizedPath);
        } else {
          filledOptionalFields.add(normalizedPath);
        }
        checkedFields.add(normalizedPath);
        filledElements.add(getParentElement(normalizedPath));
      }
    }

    int filledRequiredFieldCount = filledRequiredFields.size();
    int filledRecommendedFieldCount = filledRecommendedFields.size();
    int filledAvFieldsCount = filledAvFields(attributeValueFields);
    int filledOptionalFieldCount = filledOptionalFields.size() + filledAvFieldsCount;
    int filledElementCount = filledElements.size();
    int totalFilledFieldCount = filledRequiredFieldCount + filledRecommendedFieldCount + filledOptionalFieldCount;

    Map<FieldRequirement, Double> completionRates = new HashMap<>();
    var requiredCompleteness = ((double)filledRequiredFieldCount/requiredFieldCount) * 100;
    var recommendedCompleteness = ((double)filledRecommendedFieldCount/recommendedFieldCount) * 100;
    var optionalCompleteness = ((double) filledOptionalFieldCount/ optionalFieldCount) * 100;
    var overallCompleteness = ((double) totalFilledFieldCount / totalFieldCount) * 100;
    var elementCompleteness = ((double) filledElementCount / elementCount) * 100;
    completionRates.put(REQUIRED, requiredCompleteness);
    completionRates.put(RECOMMENDED, recommendedCompleteness);
    completionRates.put(OPTIONAL, optionalCompleteness);
    completionRates.put(OVERALL, overallCompleteness);

    return new CompletionResult(
        completionRates,
        requiredFieldCount,
        recommendedFieldCount,
        optionalFieldCount,
        totalFieldCount,
        filledRequiredFieldCount,
        filledRecommendedFieldCount,
        filledOptionalFieldCount,
        totalFilledFieldCount
    );
  }

  private boolean isRequiredField(Optional<ValueConstraints> valueConstraints){
    return valueConstraints.map(ValueConstraints::requiredValue).orElse(false);
  }

  private boolean isRecommendedField(Optional<ValueConstraints> valueConstraints){
    return valueConstraints.map(ValueConstraints::recommendedValue).orElse(false);
  }

  //RADx Metadata Specification 1.0 doesn't implement recommended option
  private boolean isRecommendedField(String fieldPath){
    return RecommendedFields.isRecommendedField(fieldPath);
  }

  private int filledAvFields(List<AttributeValueFieldValues> avFields){
    var filledAvFields = new HashSet<String>();
    for(var avField: avFields){
      var fieldValues = avField.fieldValues();
      if(!fieldsCollector.isEmptyField(fieldValues)){
        filledAvFields.add(avField.specificationPath());
      }
    }
    return filledAvFields.size();
  }

  public Collection<String> getEmptyElements(Map<String, Integer> combinedFillingReport, TemplateSchemaArtifact templateSchemaArtifact){
    Set<String> emptyElements = new HashSet<>();
    var childElements = templateSchemaArtifact.getElementKeys();
    for(var childElement: childElements){
      var currentElementArtifact = templateSchemaArtifact.getElementSchemaArtifact(childElement);
      if(isEmptyElements(combinedFillingReport, currentElementArtifact, "/" + childElement)){
        emptyElements.add(childElement);
      }
    }
    return emptyElements;
  }

  private boolean isEmptyElements(Map<String, Integer> combinedFillingReport, ElementSchemaArtifact elementSchemaArtifact, String path){
    var childFields = elementSchemaArtifact.getFieldKeys();
    var childElements = elementSchemaArtifact.getElementKeys();
    for(var childField: childFields){
      var currentPath = path + "/" + childField;
      System.out.println(currentPath);
      if(combinedFillingReport.containsKey(currentPath) && combinedFillingReport.get(currentPath) != 0){
        return false;
      }
    }

    for(var childElement:childElements){
      var childElementSchemaArtifact = elementSchemaArtifact.getElementSchemaArtifact(childElement);
      var currentPath = path + "/" + childElement;
      if(!isEmptyElements(combinedFillingReport, childElementSchemaArtifact, currentPath)){
        return false;
      }
    }
    return true;
  }

  private String getParentElement(String path){
    return path.split("/")[1];
  }

  private Collection<String> getAllElements(TemplateSchemaArtifact templateSchemaArtifact){
    var childElements = templateSchemaArtifact.getElementKeys();
    return new HashSet<>(childElements);
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
