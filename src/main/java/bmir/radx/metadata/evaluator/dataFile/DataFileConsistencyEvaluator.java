package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.StudyPhsGetter;
import com.tupilabs.human_name_parser.HumanNameParserBuilder;
import com.tupilabs.human_name_parser.HumanNameParserParser;
import com.tupilabs.human_name_parser.Name;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.CONSISTENCY;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.INCONSISTENCY;

@Component
public class DataFileConsistencyEvaluator {
    private static final String DATA_FILE_CREATORS = "Data File Creators";
    private static final String CREATOR_NAME = "Creator Name";
    private static final String CREATOR_GIVEN_NAME = "Creator Given Name";
    private static final String CREATOR_FAMILY_NAME = "Creator Family Name";
    private static final String CREATOR_TYPE = "Creator Type";
    private static final String DATA_FILE_CONTRIBUTORS = "Data File Contributors";
    private static final String CONTRIBUTOR_NAME = "Contributor Name";
    private static final String CONTRIBUTOR_GIVEN_NAME = "Contributor Given Name";
    private static final String CONTRIBUTOR_FAMILY_NAME = "Contributor Family Name";
    private static final String CONTRIBUTOR_TYPE = "Contributor Type";
    private final StudyPhsGetter studyPhsGetter;

    public DataFileConsistencyEvaluator(StudyPhsGetter studyPhsGetter) {
        this.studyPhsGetter = studyPhsGetter;
    }

    public void evaluate(Map<Path, TemplateInstanceArtifact> templateInstanceArtifacts,
                         Consumer<EvaluationResult> consumer,
                         ValidationSummary<JsonValidationResult> validationSummary){
        Set<String> inconsistentInstances = new HashSet<>();
        for(var instance : templateInstanceArtifacts.entrySet()){
            var errors = validationSummary.getValidationResults().size();
            var filePath = instance.getKey();
            var instanceArtifact = instance.getValue();
            evaluateSingleFile(filePath, instanceArtifact, validationSummary);
            if(errors < validationSummary.getValidationResults().size()) {
                var fileName = filePath.getFileName().toString();
                inconsistentInstances.add(fileName);
                validationSummary.addInvalidMetadata(fileName);
            }
        }

        int totalDataFiles = templateInstanceArtifacts.size();
        int inconsistentInstancesCount = inconsistentInstances.size();
        var rate = (double) (totalDataFiles - inconsistentInstancesCount) / totalDataFiles * 100;
        consumer.accept(new EvaluationResult(CONSISTENCY, CONSISTENT_RECORD_RATE, rate));
        consumer.accept(new EvaluationResult(CONSISTENCY, NUMBER_OF_INCONSISTENT_RECORDS, inconsistentInstancesCount));
        consumer.accept(new EvaluationResult(CONSISTENCY, INCONSISTENT_RECORDS, inconsistentInstances));
    }


    private void evaluateSingleFile(Path filePath, TemplateInstanceArtifact templateInstanceArtifact, ValidationSummary<JsonValidationResult> validationSummary){
        //evaluate name
        evaluateName(filePath, templateInstanceArtifact, validationSummary, DATA_FILE_CREATORS);
        evaluateName(filePath, templateInstanceArtifact, validationSummary, DATA_FILE_CONTRIBUTORS);
    }

    private void evaluateName(Path filePath, TemplateInstanceArtifact templateInstanceArtifact, ValidationSummary<JsonValidationResult> validationSummary, String element){
        String nameField;
        String givenNameField;
        String familyNameField;
        String typeField;
        String studyPhs = studyPhsGetter.getCleanStudyPhs(templateInstanceArtifact);
        String fileName = filePath.getFileName().toString();

        if(element.equals(DATA_FILE_CREATORS)){
            nameField = CREATOR_NAME;
            givenNameField = CREATOR_GIVEN_NAME;
            familyNameField = CREATOR_FAMILY_NAME;
            typeField = CREATOR_TYPE;
        } else{
            nameField = CONTRIBUTOR_NAME;
            givenNameField = CONTRIBUTOR_GIVEN_NAME;
            familyNameField = CONTRIBUTOR_FAMILY_NAME;
            typeField = CONTRIBUTOR_TYPE;
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
                //Using HumanNameParse Library
                Name fullName = new Name(name.get());
                HumanNameParserBuilder builder = new HumanNameParserBuilder(fullName);
                HumanNameParserParser parser = builder.build();
                String parsedFirstName = parser.getFirst();
                String parsedFamilyName = parser.getLast();
                String parsedMiddleName = parser.getMiddle();
                String parsedGivenName = parsedFirstName + " " + parsedMiddleName;

                processIncorrectName(name.get(), Optional.of(parsedFamilyName), familyName, i, studyPhs, fileName, element, familyNameField, validationSummary);
                processIncorrectName(name.get(), Optional.of(parsedGivenName), givenName, i, studyPhs, fileName, element, givenNameField, validationSummary);
            }
            i++;
        }
    }

    private void processIncorrectName(String fullName, Optional<String> parsedName, Optional<String> providedName, Integer i, String studyPhs, String fileName, String elementName, String fieldName, ValidationSummary<JsonValidationResult> validationSummary){
        if(!isCorrectName(parsedName, providedName)){
            var pointer = getPointer(i, elementName, fieldName);
            var errorMessage = getErrorMessage(fullName, providedName.orElse(null), fieldName);
            validationSummary.updateValidationResult(
                new JsonValidationResult(studyPhs, fileName, pointer, INCONSISTENCY, errorMessage, parsedName.orElse(null), providedName.get())
            );
        }
    }

    private Map<String, Optional<String>> parseFullName(String fullName, String element) {
        String givenNameField;
        String familyNameField;
        if (element.equals(DATA_FILE_CREATORS)) {
            givenNameField = CREATOR_GIVEN_NAME;
            familyNameField = CREATOR_FAMILY_NAME;
        } else {
            givenNameField = CONTRIBUTOR_GIVEN_NAME;
            familyNameField = CONTRIBUTOR_FAMILY_NAME;
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
                i +
                "]/" +
                field;
    }
    private String getErrorMessage(String fullName, String providedName, String filed){
        return filed + " [" + providedName + "] is inconsistent with the provided full name: [" + fullName + "]";
    }
}
