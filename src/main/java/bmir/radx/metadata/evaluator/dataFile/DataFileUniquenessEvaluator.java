package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.util.StudyPhsGetter;
import org.metadatacenter.artifacts.model.core.InstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationCriterion.UNIQUENESS;
import static bmir.radx.metadata.evaluator.EvaluationMetric.*;
import static bmir.radx.metadata.evaluator.util.IssueTypeMapping.IssueType.DUPLICATE_RECORD;

@Component
public class DataFileUniquenessEvaluator {
  private final StudyPhsGetter studyPhsGetter;

  public DataFileUniquenessEvaluator(StudyPhsGetter studyPhsGetter) {
    this.studyPhsGetter = studyPhsGetter;
  }


  public void evaluate(Map<Path, TemplateInstanceArtifact> templateInstanceArtifacts,
                       Consumer<EvaluationResult> consumer,
                       ValidationSummary<JsonValidationResult> validationSummary){

    var duplicatesMap = checkDuplicates(templateInstanceArtifacts);

    Set<String> duplicates = new HashSet<>();
    for(var entry: duplicatesMap.entrySet()){
      var paths = entry.getValue();
      if (paths.size() > 1) { // If have duplicates
        duplicates.addAll(paths);
        //add to invalid metadata list and update validation results
        validationSummary.addMultiInvalidMetadata(paths);
        var filePath = entry.getKey();
        var fileName = filePath.getFileName().toString();
        var studyPhs = studyPhsGetter.getCleanStudyPhs(templateInstanceArtifacts.get(filePath));
        validationSummary.updateValidationResult(new JsonValidationResult(studyPhs, fileName,null, DUPLICATE_RECORD, paths.toString(), null));
      }
    }

    //update evaluation report
    int totalDataFiles = templateInstanceArtifacts.size();
    int duplicatesCount = duplicates.size();
    var rate = (double) (totalDataFiles - duplicatesCount) / totalDataFiles * 100;
    consumer.accept(new EvaluationResult(UNIQUENESS, UNIQUENESS_RATE, rate));
    consumer.accept(new EvaluationResult(UNIQUENESS, DUPLICATE_RECORDS_NUMBER, duplicatesCount));
    consumer.accept(new EvaluationResult(UNIQUENESS, DUPLICATE_RECORDS, duplicates));
  }

  private Map<Path, List<String>> checkDuplicates(Map<Path, TemplateInstanceArtifact> templateInstanceArtifacts) {
    Map<Path, List<String>> identicalGroups = new HashMap<>();
    Set<Path> visited = new HashSet<>();

    // Convert entry set for easy access to both path and instance
    List<Map.Entry<Path, TemplateInstanceArtifact>> entries = new ArrayList<>(templateInstanceArtifacts.entrySet());

    for (int i = 0; i < entries.size(); i++) {
      var entry1 = entries.get(i);
      Path path1 = entry1.getKey();
      var instance1 = entry1.getValue();

      if (visited.contains(path1)) {
        continue;
      }

      List<String> duplicates = new ArrayList<>();

      for (int j = i + 1; j < entries.size(); j++) {
        var entry2 = entries.get(j);
        Path path2 = entry2.getKey();
        var instance2 = entry2.getValue();

        if (visited.contains(path2)) {
          continue;
        }

        // Check if the two instances are identical
        if (areIdentical(instance1, instance2)) {
          duplicates.add(path2.toString());
          visited.add(path2);
        }
      }

      // If duplicates are found, add the current path and its group to identicalGroups
      if (!duplicates.isEmpty()) {
        duplicates.add(0, path1.toString()); // Add the original path to the beginning of the group
        identicalGroups.put(path1, duplicates);
        visited.add(path1); // Mark the original path as visited
      }
    }

    return identicalGroups;
  }

  public void checkDuplicatesInInstance(TemplateInstanceArtifact templateInstanceArtifact, Consumer<EvaluationResult> handler){
    var multiInstanceFieldInstances = templateInstanceArtifact.multiInstanceFieldInstances();
    var multiInstanceElementInstances = templateInstanceArtifact.multiInstanceElementInstances();

    int duplicateCount = 0;
    var duplicateElementInstances = new HashMap<String, String>();
    for(var artifact : multiInstanceFieldInstances.entrySet()){
      var comparisonResult = areAllUnique(artifact.getValue());
      if(!comparisonResult.areAllUnique()){
        duplicateCount++;
        duplicateElementInstances.put(artifact.getKey(), comparisonResult.toString());
      }
    }

    for(var artifact : multiInstanceElementInstances.entrySet()){
      var comparisonResult = areAllUnique(artifact.getValue());
      if(!comparisonResult.areAllUnique()){
        duplicateCount++;
        duplicateElementInstances.put(artifact.getKey(), comparisonResult.toString());
      }
    }

    handler.accept(new EvaluationResult(UNIQUENESS, DUPLICATE_ELEMENT_INSTANCES_COUNT, duplicateCount));
    handler.accept(new EvaluationResult(UNIQUENESS, DUPLICATE_ELEMENT_INSTANCES, getDuplicateElementInstances(duplicateElementInstances)));
  }

  private boolean areIdentical(InstanceArtifact f1, InstanceArtifact f2){
    if(f1 == f2){
      return true;
    }

    if(f1 == null || f2 == null){
      return false;
    }

    if(!f1.getClass().equals(f2.getClass())){
      return false;
    }

    return f1.equals(f2);
  }

  private boolean areIdentical(TemplateInstanceArtifact t1, TemplateInstanceArtifact t2){
    String dataFileIdentity = "Data File Identity";
    String fileName = "File Name";
    String version = "Version";
    var fileName1 = t1.singleInstanceElementInstances().get(dataFileIdentity).singleInstanceFieldInstances().get(fileName).jsonLdValue();
    var fileName2 = t2.singleInstanceElementInstances().get(dataFileIdentity).singleInstanceFieldInstances().get(fileName).jsonLdValue();
    var version1 = t1.singleInstanceElementInstances().get(dataFileIdentity).singleInstanceFieldInstances().get(version).jsonLdValue();
    var version2 = t2.singleInstanceElementInstances().get(dataFileIdentity).singleInstanceFieldInstances().get(version).jsonLdValue();

    return fileName1.equals(fileName2) && version1.equals(version2);
  }

  public <T extends InstanceArtifact> ComparisonResult areAllUnique(List<T> artifacts) {
    List<List<Integer>> identicalGroups = new ArrayList<>();
    boolean allUnique = true;
    Set<Integer> visited = new HashSet<>();

    for (int i = 0; i < artifacts.size(); i++) {
      if (visited.contains(i))
        continue;
      List<Integer> group = new ArrayList<>();
      for (int j = i + 1; j < artifacts.size(); j++) {
        if (visited.contains(j))
          continue;
        if (areIdentical(artifacts.get(i), artifacts.get(j))) {
          allUnique = false;
          group.add(j);
          visited.add(j);
        }
      }
      if (!group.isEmpty()) {
        group.add(0, i); // Add the current index to the group
        identicalGroups.add(group);
        visited.add(i);
      }
    }

    return new ComparisonResult(allUnique, identicalGroups);
  }


  private String getDuplicateElementInstances(HashMap<String, String> duplicates){
    if (duplicates.isEmpty()) {
      return "null";
    } else {
      StringBuilder sb = new StringBuilder();
      for (Map.Entry<String, String> entry : duplicates.entrySet()) {
        sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
      }
      if (sb.length() > 0) {
        sb.setLength(sb.length() - 2);
      }
      return sb.toString();
    }
  }
}
