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
import java.util.Optional;
import java.util.function.Consumer;

@Component
public class DataFileEvaluator implements Evaluator<JsonValidationResult> {
  private final Logger logger = LoggerFactory.getLogger(DataFileEvaluator.class);
  private final DataFileMetadataReader dataFileMetadataReader;
  private final DataFileCompletenessEvaluator completenessEvaluator;
  private final DataFileValidityEvaluator validityEvaluator;
  private final DataFileVocabularyEvaluator dataFileVocabularyEvaluator;
  private final DataFileAccessibilityEvaluator accessibilityEvaluator;
  private final DataFileConsistencyEvaluator consistencyEvaluator;
  private final DataFileUniquenessEvaluator uniquenessEvaluator;
  private final DataFileAccuracyEvaluator accuracyEvaluator;
  private final DataFileLingQualityEvaluator lingQualityEvaluator;

  public DataFileEvaluator(DataFileMetadataReader dataFileMetadataReader,
                           DataFileCompletenessEvaluator completenessEvaluator,
                           DataFileValidityEvaluator validityEvaluator,
                           DataFileVocabularyEvaluator dataFileVocabularyEvaluator,
                           DataFileAccessibilityEvaluator accessibilityEvaluator,
                           DataFileConsistencyEvaluator consistencyEvaluator,
                           DataFileUniquenessEvaluator uniquenessEvaluator,
                           DataFileAccuracyEvaluator accuracyEvaluator,
                           DataFileLingQualityEvaluator lingQualityEvaluator) {
    this.dataFileMetadataReader = dataFileMetadataReader;
    this.completenessEvaluator = completenessEvaluator;
    this.validityEvaluator = validityEvaluator;
    this.dataFileVocabularyEvaluator = dataFileVocabularyEvaluator;
    this.accessibilityEvaluator = accessibilityEvaluator;
    this.consistencyEvaluator = consistencyEvaluator;
    this.uniquenessEvaluator = uniquenessEvaluator;
    this.accuracyEvaluator = accuracyEvaluator;
    this.lingQualityEvaluator = lingQualityEvaluator;
  }

  public EvaluationReport<JsonValidationResult> evaluate(Path... filePaths){
    int numberOfPaths = filePaths.length;
    Path dataFilePath = filePaths[0];

    var results = new ArrayList<EvaluationResult>();
    Consumer<EvaluationResult> consumer = results::add;

    var metadataInstances = dataFileMetadataReader.readDataFileMetadata(dataFilePath);
    var metadataInstancesList = new ArrayList<>(metadataInstances.values());
    var validationResults = new ArrayList<JsonValidationResult>();
    var invalidMetadata = new HashSet<String>();
    var validationReport = new ValidationSummary<>(validationResults, invalidMetadata);

    logger.info("Start to evaluate the completeness of data file metadata");
    completenessEvaluator.evaluate(metadataInstances, consumer, validationReport);

    logger.info("Start to evaluate the vocabularies of data file metadata");
    dataFileVocabularyEvaluator.evaluate(metadataInstancesList, consumer);

    logger.info("Start to evaluate the accessibility of data file metadata");
    accessibilityEvaluator.evaluate(metadataInstances, consumer, validationReport);

    //If study path is also provided, apply cross-check of metadata between study and data file metadata
    Optional<Path> studyPath = (numberOfPaths > 1) ? Optional.of(filePaths[1]) : Optional.empty();

    logger.info("Start to evaluate the accuracy of data file metadata");
    accuracyEvaluator.evaluate(studyPath, metadataInstances, consumer, validationReport);

    logger.info("Start to evaluate the consistency of data file metadata");
    consistencyEvaluator.evaluate(studyPath, metadataInstances, consumer, validationReport);

    logger.info("Start to evaluate the uniqueness of data file metadata");
    uniquenessEvaluator.evaluate(metadataInstances, consumer, validationReport);

    logger.info("Start to evaluate the validity of data file metadata");
    validityEvaluator.evaluate(metadataInstances, consumer, validationReport);

    logger.info("Start to evaluate the linguistic quality of data file metadata");
    lingQualityEvaluator.evaluate(metadataInstances, consumer, validationReport);

    return new EvaluationReport<>(results, validationReport.getValidationResults());
  }
}
