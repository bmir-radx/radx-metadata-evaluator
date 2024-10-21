package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import edu.stanford.bmir.radx.metadata.validator.lib.*;
import org.metadatacenter.artifacts.model.core.ElementSchemaArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.core.fields.constraints.ValueConstraints;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.BASIC_INFO;
import static bmir.radx.metadata.evaluator.EvaluationCriterion.COMPLETENESS;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

@Component
public class DataFileCompletenessEvaluator {
  private final FieldsCollector fieldsCollector;

  public DataFileCompletenessEvaluator(FieldsCollector fieldsCollector) {
    this.fieldsCollector = fieldsCollector;
  }

  public void evaluate(TemplateSchemaArtifact templateSchemaArtifact, TemplateInstanceValuesReporter templateInstanceValuesReporter, Consumer<EvaluationResult> handler){
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

    var requiredCompleteness = ((double)filledRequiredFieldCount/requiredFieldCount) * 100;
    var recommendedCompleteness = ((double)filledRecommendedFieldCount/recommendedFieldCount) * 100;
    var optionalCompleteness = ((double) filledOptionalFieldCount/ optionalFieldCount) * 100;
    var overallCompleteness = ((double) totalFilledFieldCount / totalFieldCount) * 100;
    var elementCompleteness = ((double) filledElementCount / elementCount) * 100;

    handler.accept(new EvaluationResult(BASIC_INFO, TOTAL_REQUIRED_FIELDS, String.valueOf(requiredFieldCount)));
    handler.accept(new EvaluationResult(COMPLETENESS, FILLED_REQUIRED_FIELDS_COUNT, String.valueOf(filledRequiredFieldCount)));
    handler.accept(new EvaluationResult(COMPLETENESS, FILLED_REQUIRED_FIELDS, filledRequiredFields.toString()));
    handler.accept(new EvaluationResult(COMPLETENESS, REQUIRED_FIELDS_COMPLETION_RATE, String.format("%.2f%%",requiredCompleteness)));

    handler.accept(new EvaluationResult(BASIC_INFO,TOTAL_RECOMMENDED_FIELDS, String.valueOf(recommendedFieldCount)));
    handler.accept(new EvaluationResult(COMPLETENESS, FILLED_RECOMMENDED_FIELDS_COUNT,String.valueOf(filledRecommendedFieldCount)));
    handler.accept(new EvaluationResult(COMPLETENESS, FILLED_RECOMMENDED_FIELDS, filledRecommendedFields.toString()));
    handler.accept(new EvaluationResult(COMPLETENESS, RECOMMENDED_FIELDS_COMPLETION_RATE, String.format("%.2f%%",recommendedCompleteness)));

    handler.accept(new EvaluationResult(BASIC_INFO,TOTAL_OPTIONAL_FIELDS, String.valueOf(optionalFieldCount)));
    handler.accept(new EvaluationResult(COMPLETENESS, FILLED_OPTIONAL_FIELDS_COUNT, String.valueOf(filledOptionalFieldCount)));
    handler.accept(new EvaluationResult(COMPLETENESS, FILLED_OPTIONAL_FIELDS, filledOptionalFields.toString()));
    handler.accept(new EvaluationResult(COMPLETENESS, OPTIONAL_FIELDS_COMPLETION_RATE, String.format("%.2f%%",optionalCompleteness)));

    handler.accept(new EvaluationResult(BASIC_INFO, TOTAL_FIELDS, String.valueOf(totalFieldCount)));
    handler.accept(new EvaluationResult(COMPLETENESS, TOTAL_FILLED_FIELDS, String.valueOf(totalFilledFieldCount)));
    handler.accept(new EvaluationResult(COMPLETENESS, OVERALL_COMPLETION_RATE, String.format("%.2f%%",overallCompleteness)));

    handler.accept(new EvaluationResult(COMPLETENESS, FILLED_ELEMENTS, filledElements.toString()));
    handler.accept(new EvaluationResult(COMPLETENESS, FILLED_ELEMENTS_COUNT, String.valueOf(filledElementCount)));
    handler.accept(new EvaluationResult(COMPLETENESS, ELEMENT_COMPLETION_RATE, String.format("%.2f%%",elementCompleteness)));
  }

  private boolean isRequiredField(Optional<ValueConstraints> valueConstraints){
    return valueConstraints.map(ValueConstraints::requiredValue).orElse(false);
  }

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
}
