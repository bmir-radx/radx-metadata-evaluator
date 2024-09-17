package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.EvaluationResult;
import com.fasterxml.jackson.databind.node.ObjectNode;
import edu.stanford.bmir.radx.metadata.validator.lib.JsonLoader;
import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
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
  private final LinkEvaluator linkEvaluator;
  private final UniquenessEvaluator uniquenessEvaluator;

  public SingleFileEvaluator(DataFileCompletenessEvaluator dataFileCompletenessEvaluator, ValidityEvaluator validityEvaluator, ControlledTermsEvaluator controlledTermsEvaluator, LinkEvaluator linkEvaluator, UniquenessEvaluator uniquenessEvaluator) {
    this.dataFileCompletenessEvaluator = dataFileCompletenessEvaluator;
    this.validityEvaluator = validityEvaluator;
    this.controlledTermsEvaluator = controlledTermsEvaluator;
    this.linkEvaluator = linkEvaluator;
    this.uniquenessEvaluator = uniquenessEvaluator;
  }

  public EvaluationReport evaluate(String templateContent, String instanceContent){
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
    validityEvaluator.evaluate(templateContent, instanceContent, consumer);
    controlledTermsEvaluator.evaluate(templateReporter, templateInstanceValuesReporter, consumer);
    linkEvaluator.evaluate(templateReporter, templateInstanceValuesReporter, consumer);
    uniquenessEvaluator.evaluate(templateInstanceArtifact, consumer);

    return new EvaluationReport(results);
  }
}
