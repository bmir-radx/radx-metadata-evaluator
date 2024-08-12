package bmir.radx.metadata.evaluator.variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class VariablesProcessor {
  public Map<String, Map<String, HashSet<String>>> processAllVariables(List<AllVariablesRow> allVariableRows){
    Map<String, Map<String, HashSet<String>>> variable2StudyAndFile = new HashMap<>();

    for (AllVariablesRow row : allVariableRows) {
      String phsId = row.phsId();
      String fileName = row.fileName();

      for (String variable : row.variables()) {
        variable2StudyAndFile
            .computeIfAbsent(variable, k -> new HashMap<>())
            .computeIfAbsent("studies", k -> new HashSet<>())
            .add(phsId);

        variable2StudyAndFile
            .get(variable)
            .computeIfAbsent("files", k -> new HashSet<>())
            .add(fileName);
      }
    }

    return variable2StudyAndFile;
  }
}
