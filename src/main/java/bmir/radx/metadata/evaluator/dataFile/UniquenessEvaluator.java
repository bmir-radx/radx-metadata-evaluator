package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationCriterion;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import org.metadatacenter.artifacts.model.core.InstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

@Component
public class UniquenessEvaluator {
  public void evaluate(TemplateInstanceArtifact templateInstanceArtifact, Consumer<EvaluationResult> handler){
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

    handler.accept(new EvaluationResult(EvaluationCriterion.UNIQUENESS, DUPLICATE_ELEMENT_INSTANCES_COUNT, String.valueOf(duplicateCount)));
    handler.accept(new EvaluationResult(EvaluationCriterion.UNIQUENESS, DUPLICATE_ELEMENT_INSTANCES, getDuplicateElementInstances(duplicateElementInstances)));
  }

  private boolean areIdentical(InstanceArtifact f1, InstanceArtifact f2){
    if(f1 == f2){
      return true;
    }

    if(f1 == null || f2 == null){
      return false;
    }

    // Type check
    if(!f1.getClass().equals(f2.getClass())){
      return false;
    }

    return f1.equals(f2);
  }

  public <T extends InstanceArtifact> ComparisonResult areAllUnique(List<T> artifacts) {
    List<List<Integer>> identicalGroups = new ArrayList<>();
    boolean allUnique = true;
    Set<Integer> visited = new HashSet<>();

    for (int i = 0; i < artifacts.size(); i++) {
      if (visited.contains(i))
        continue; // Skip if already visited
      List<Integer> group = new ArrayList<>();
      for (int j = i + 1; j < artifacts.size(); j++) {
        if (visited.contains(j))
          continue; // Skip if already visited
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
