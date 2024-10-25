package bmir.radx.metadata.evaluator.dataFile;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.sharedComponents.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;

@Component
public class DataFileEvaluator implements Evaluator<JsonValidationResult> {
  private final Logger logger = LoggerFactory.getLogger(DataFileEvaluator.class);
  private final DataFileMetadataReader dataFileMetadataReader;
  private final DataFileCompletenessEvaluator completenessEvaluator;
  private final DataFileValidityEvaluator validityEvaluator;
  private final DataFileVocabularyEvaluator dataFileVocabularyEvaluator;
  private final DataFileAccessibilityEvaluator accessibilityEvaluator;

  public DataFileEvaluator(DataFileMetadataReader dataFileMetadataReader,
                           DataFileCompletenessEvaluator completenessEvaluator,
                           DataFileValidityEvaluator validityEvaluator,
                           DataFileVocabularyEvaluator dataFileVocabularyEvaluator,
                           DataFileAccessibilityEvaluator accessibilityEvaluator) {
    this.dataFileMetadataReader = dataFileMetadataReader;
    this.completenessEvaluator = completenessEvaluator;
    this.validityEvaluator = validityEvaluator;
    this.dataFileVocabularyEvaluator = dataFileVocabularyEvaluator;
    this.accessibilityEvaluator = accessibilityEvaluator;
  }

  public EvaluationReport<JsonValidationResult> evaluate(Path filepath){
    var results = new ArrayList<EvaluationResult>();
    Consumer<EvaluationResult> consumer = results::add;

    var metadataInstances = dataFileMetadataReader.readDataFileMetadata(filepath);
    var metadataInstancesList = new ArrayList<>(metadataInstances.values());
    var validationResults = new ArrayList<JsonValidationResult>();
    var invalidMetadata = new HashSet<String>();
    var validationReport = new ValidationSummary<>(validationResults, invalidMetadata);

    logger.info("Start to evaluate the completeness of data file metadata");
    completenessEvaluator.evaluate(metadataInstancesList, consumer);
    logger.info("Start to evaluate the vocabularies of data file metadata");
    dataFileVocabularyEvaluator.evaluate(metadataInstancesList, consumer);
    logger.info("Start to evaluate the accessibility of data file metadata");
    accessibilityEvaluator.evaluate(metadataInstances, consumer, validationReport);
    logger.info("Start to evaluate the validity of data file metadata");
    validityEvaluator.evaluate(metadataInstances, consumer, validationReport);

    return new EvaluationReport<>(results, validationReport.getValidationResults());
  }

}
