package bmir.radx.metadata.evaluator.variable;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.SpreadsheetReader;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.sharedComponents.Evaluator;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Component
public class VariableEvaluator implements Evaluator<SpreadsheetValidationResult> {
  private final VariableCompletenessEvaluator completenessEvaluator;
  private final CoreCdeEvaluator coreCdeEvaluator;
  private final ConsistentEvaluator consistentEvaluator;

  public VariableEvaluator(VariableCompletenessEvaluator completenessEvaluator, CoreCdeEvaluator coreCdeEvaluator, ConsistentEvaluator consistentEvaluator) {
    this.completenessEvaluator = completenessEvaluator;
    this.coreCdeEvaluator = coreCdeEvaluator;
    this.consistentEvaluator = consistentEvaluator;
  }

  public EvaluationReport<SpreadsheetValidationResult> evaluate(Path... filePaths) {
//  public EvaluationReport evaluateSingleDataFile(FileInputStream integratedFileInputStream) {
    Path metadataFilePath = filePaths[0];
    var results = new ArrayList<EvaluationResult>();
    Consumer<EvaluationResult> consumer = results::add;
    var variableMetadataReader = new SpreadsheetReader();

    try {
      var variableMetadataRows = variableMetadataReader.readVariablesMetadata(metadataFilePath);
      var allVariablesRows = variableMetadataReader.readAllVariables(metadataFilePath);
      completenessEvaluator.evaluate(variableMetadataRows, consumer);
      coreCdeEvaluator.evaluate(variableMetadataRows, consumer);
      consistentEvaluator.evaluate(variableMetadataRows, allVariablesRows, consumer);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new EvaluationReport<>(results, List.of());
  }
}
