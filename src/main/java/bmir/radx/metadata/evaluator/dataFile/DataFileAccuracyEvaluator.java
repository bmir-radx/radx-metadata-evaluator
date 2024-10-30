package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.ACCURACY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.INACCURATE_RECORD;

@Component
public class DataFileAccuracyEvaluator {
    private final String creators = "Data File Creators";
    private final String creatorName = "Creator Name";
    private final String creatorGivenName = "Creator Given Name";
    private final String creatorFamilyName = "Creator Family Name";
    private final String creatorType = "Creator Type";
    private final String contributors = "Data File Contributors";
    private final String contributorName = "Contributor Name";
    private final String contributorGivenName = "Contributor Given Name";
    private final String contributorFamilyName = "Contributor Family Name";
    private final String contributorType = "Contributor Type";

    public void evaluate(Map<Path, TemplateInstanceArtifact> templateInstanceArtifacts,
                         Consumer<EvaluationResult> consumer,
                         ValidationSummary<JsonValidationResult> validationSummary){
        Set<String> inaccurateInstances = new HashSet<>();
        for(var instance : templateInstanceArtifacts.entrySet()){
            var errors = validationSummary.getValidationResults().size();
            var filePath = instance.getKey();
            var instanceArtifact = instance.getValue();
            evaluateSingleFile(filePath, instanceArtifact, validationSummary);
            if(errors < validationSummary.getValidationResults().size()) {
                var fileName = filePath.getFileName().toString();
                inaccurateInstances.add(fileName);
                validationSummary.addInvalidMetadata(fileName);
            }
        }

        int totalDataFiles = templateInstanceArtifacts.size();
        int inaccurateDataFiles = inaccurateInstances.size();
        var rate = (double) (totalDataFiles - inaccurateDataFiles) / totalDataFiles * 100;
        consumer.accept(new EvaluationResult(ACCURACY, ACCURACY_RATE, rate));
        consumer.accept(new EvaluationResult(ACCURACY, INACCURATE_DATA_FILES, inaccurateInstances));
    }


    private void evaluateSingleFile(Path filePath, TemplateInstanceArtifact templateInstanceArtifact, ValidationSummary<JsonValidationResult> validationSummary){
        //evaluate name
        evaluateName(filePath, templateInstanceArtifact, validationSummary, creators);
        evaluateName(filePath, templateInstanceArtifact, validationSummary, contributors);
    }

    private void evaluateName(Path filePath, TemplateInstanceArtifact templateInstanceArtifact, ValidationSummary<JsonValidationResult> validationSummary, String element){
        String nameField;
        String givenNameField;
        String familyNameField;
        String typeField;
        String fieldName;

        if(element.equals(creators)){
            nameField = creatorName;
            givenNameField = creatorGivenName;
            familyNameField = creatorFamilyName;
            typeField = creatorType;
        } else{
            nameField = contributorName;
            givenNameField = contributorGivenName;
            familyNameField = contributorFamilyName;
            typeField = contributorType;
        }

        var elementArtifacts = templateInstanceArtifact.multiInstanceElementInstances().get(element);
        int i = 0;
        for(var elementArtifact: elementArtifacts){
            var fields = elementArtifact.singleInstanceFieldInstances();
            var name = fields.get(nameField).jsonLdValue();
            var givenName = fields.get(givenNameField).jsonLdValue();
            var familyName = fields.get(familyNameField).jsonLdValue();
            var type = fields.get(typeField).label();
            if(name.isPresent() && type.equals(Optional.of("Person"))){
                var nameParts = parseFullName(name.get(), element);
                var parsedGivenName = nameParts.get(givenNameField);
                var parsedFamilyName = nameParts.get(familyNameField);
                if(!isCorrectName(parsedFamilyName, familyName)){
                    fieldName = familyNameField;
                    processIncorrectName(name.get(), parsedFamilyName, familyName, i, filePath.toString(), element, fieldName, validationSummary);
                }
                if(!isCorrectName(parsedGivenName, givenName)){
                    fieldName = givenNameField;
                    processIncorrectName(name.get(), parsedGivenName, givenName, i, filePath.toString(), element, fieldName, validationSummary);
                }
            }
            i++;
        }
    }

    private void processIncorrectName(String fullName, Optional<String> parsedName, Optional<String> providedName, Integer i, String filePath, String elementName, String fieldName, ValidationSummary<JsonValidationResult> validationSummary){
        if(!isCorrectName(parsedName, providedName)){
            var pointer = getPointer(i, elementName, fieldName);
            var errorMessage = getErrorMessage(fullName, providedName.orElse(null), fieldName);
            validationSummary.updateValidationResults(List.of(new JsonValidationResult(filePath, pointer, INACCURATE_RECORD, errorMessage, parsedName.orElse(null))));
        }
    }

    private Map<String, Optional<String>> parseFullName(String fullName, String element) {
        String givenNameField;
        String familyNameField;
        if (element.equals(creators)) {
            givenNameField = creatorGivenName;
            familyNameField = creatorFamilyName;
        } else {
            givenNameField = contributorGivenName;
            familyNameField = contributorFamilyName;
        }

        // Remove suffixes like ", MD", ", PHD", ", II" and prefixes like "Dr. "
        fullName = fullName.replaceAll("(?i),\\s*(MD|PHD|II)", "").replaceAll("(?i)Dr\\.\\s*", "").trim();

        Map<String, Optional<String>> nameParts = new HashMap<>();
        String[] nameTokens;

        // Check if the name is in "Last, First Middle" format
        if (fullName.contains(",")) {
            nameTokens = fullName.split(",\s*");
            if (nameTokens.length == 2) {
                nameParts.put(familyNameField, Optional.of(nameTokens[0].trim()));
                nameParts.put(givenNameField, Optional.of(nameTokens[1].trim()));
            } else {
                nameParts.put(givenNameField, Optional.of(fullName.trim()));
                nameParts.put(familyNameField, Optional.empty());
            }
        } else {
            // "First Middle Last" format
            nameTokens = fullName.trim().split("\\s+");
            if (nameTokens.length == 1) {
                nameParts.put(givenNameField, Optional.of(nameTokens[0]));
                nameParts.put(familyNameField, Optional.empty());
            } else if (nameTokens.length >= 2) {
                StringBuilder givenName = new StringBuilder();
                for (int i = 0; i < nameTokens.length - 1; i++) {
                    givenName.append(nameTokens[i]).append(" ");
                }
                nameParts.put(givenNameField, Optional.of(givenName.toString().trim()));
                nameParts.put(familyNameField, Optional.of(nameTokens[nameTokens.length - 1]));
            }
        }

        return nameParts;
    }

    private boolean isCorrectName(Optional<String> parsedName, Optional<String> providedName){
        return parsedName.equals(providedName);
    }

    private String getPointer(int i, String element, String field){
        return element +
                "[" +
                String.valueOf(i) +
                "]/" +
                field;
    }
    private String getErrorMessage(String fullName, String providedName, String filed){
        return filed + " - " + providedName + " is incorrect for the provided full name: " + fullName;
    }


}
