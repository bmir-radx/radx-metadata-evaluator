package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.sharedComponents.LinkChecker;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.bmir.radx.metadata.validator.lib.JsonLoader;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.function.Consumer;

@Component
public class SingleFileEvaluator {
  private final DataFileCompletenessEvaluator dataFileCompletenessEvaluator;
  private final ValidityEvaluator validityEvaluator;
  private final ControlledTermsEvaluator controlledTermsEvaluator;
  private final LinkChecker linkChecker;
  private final UniquenessEvaluator uniquenessEvaluator;

  public SingleFileEvaluator(DataFileCompletenessEvaluator dataFileCompletenessEvaluator, ValidityEvaluator validityEvaluator, ControlledTermsEvaluator controlledTermsEvaluator, LinkChecker linkChecker, UniquenessEvaluator uniquenessEvaluator) {
    this.dataFileCompletenessEvaluator = dataFileCompletenessEvaluator;
    this.validityEvaluator = validityEvaluator;
    this.controlledTermsEvaluator = controlledTermsEvaluator;
    this.linkChecker = linkChecker;
    this.uniquenessEvaluator = uniquenessEvaluator;
  }

  public EvaluationReport<JsonValidationResult> evaluate(String templateContent, String instanceContent){
    var results = new ArrayList<EvaluationResult>();
    Consumer<EvaluationResult> consumer = results::add;

    var templateNode = JsonLoader.loadJson(templateContent, "Template");
    var instanceNode = JsonLoader.loadJson(instanceContent, "Instance");

    var jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    var templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    var templateReporter = new TemplateReporter(templateSchemaArtifact);

    var templateInstanceArtifact = jsonSchemaArtifactReader.readTemplateInstanceArtifact((ObjectNode) instanceNode);
    var templateInstanceValuesReporter = new TemplateInstanceValuesReporter(templateInstanceArtifact);

    dataFileCompletenessEvaluator.evaluate(templateSchemaArtifact, templateInstanceValuesReporter, consumer);
    var validationResult = validityEvaluator.evaluate(templateContent, instanceContent, consumer);
    controlledTermsEvaluator.evaluate(templateReporter, templateInstanceValuesReporter, consumer);
    linkChecker.evaluate(templateReporter, templateInstanceValuesReporter, consumer);
    uniquenessEvaluator.evaluate(templateInstanceArtifact, consumer);

    return new EvaluationReport<>(results, validationResult);
  }
}
