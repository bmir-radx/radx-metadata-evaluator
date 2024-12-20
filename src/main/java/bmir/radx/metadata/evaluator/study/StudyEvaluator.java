package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.SpreadsheetReader;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import bmir.radx.metadata.evaluator.result.ValidationSummary;
import bmir.radx.metadata.evaluator.sharedComponents.Evaluator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.function.Consumer;

@Component
public class StudyEvaluator implements Evaluator<SpreadsheetValidationResult> {
  private final Logger logger = LoggerFactory.getLogger(StudyEvaluator.class);
  private final StudyCompletenessEvaluator completenessEvaluator;
  private final StudyConsistencyEvaluator consistencyEvaluator;
  private final StudyAccuracyEvaluator accuracyEvaluator;
  private final StudyValidityEvaluator studyValidityEvaluator;
  private final StudyAccessibilityEvaluator studyAccessibilityEvaluator;
  private final StudyUniquenessEvaluator uniquenessEvaluator;
  private final StudyGrammarChecker grammarChecker;
  private final StudyCodeListEvaluator codeListEvaluator;

  public StudyEvaluator(StudyCompletenessEvaluator completenessEvaluator,
                        StudyConsistencyEvaluator consistencyEvaluator,
                        StudyAccuracyEvaluator accuracyEvaluator,
                        StudyValidityEvaluator studyValidityEvaluator,
                        StudyAccessibilityEvaluator studyAccessibilityEvaluator,
                        StudyUniquenessEvaluator uniquenessEvaluator,
                        StudyGrammarChecker grammarChecker,
                        StudyCodeListEvaluator codeListEvaluator) {
    this.completenessEvaluator = completenessEvaluator;
    this.consistencyEvaluator = consistencyEvaluator;
    this.accuracyEvaluator = accuracyEvaluator;
    this.studyValidityEvaluator = studyValidityEvaluator;
    this.studyAccessibilityEvaluator = studyAccessibilityEvaluator;
    this.uniquenessEvaluator = uniquenessEvaluator;
    this.grammarChecker = grammarChecker;
    this.codeListEvaluator = codeListEvaluator;
  }

  public EvaluationReport<SpreadsheetValidationResult> evaluate(Path... filePaths) {
    Path metadataFilePath = filePaths[0];
    var evaluationResults = new ArrayList<EvaluationResult>();
    Consumer<EvaluationResult> consumer = evaluationResults::add;

    var studyMetadataReader = new SpreadsheetReader();
    var validationResults = new ArrayList<SpreadsheetValidationResult>();
    var invalidStudy = new HashSet<String>();
    var validationSummary = new ValidationSummary<>(validationResults, invalidStudy);
    try {
      var studyMetadataRows = studyMetadataReader.readStudyMetadata(metadataFilePath);

//      logger.info("Start to check completeness of study metadata spreadsheet");
//      completenessEvaluator.evaluate(studyMetadataRows, consumer);
//
//      logger.info("Start to check links resolvability of study metadata spreadsheet");
////      studyAccessibilityEvaluator.evaluate(studyMetadataRows, consumer, validationSummary);
//
//      logger.info("Start to check accuracy of study metadata spreadsheet");
//      accuracyEvaluator.evaluate(studyMetadataRows, consumer, validationSummary);
//
      logger.info("Start to check consistency of study metadata spreadsheet");
      consistencyEvaluator.evaluate(studyMetadataRows, consumer, validationSummary);
//
//      logger.info("Start to check uniqueness of study metadata spreadsheet");
//      uniquenessEvaluator.evaluate(studyMetadataRows, consumer, validationSummary);
//
//      logger.info("Start to check validity of study metadata spreadsheet");
//      studyValidityEvaluator.evaluate(metadataFilePath, studyMetadataRows, consumer, validationSummary);

      logger.info("Start to check controlled terms of study metadata spreadsheet");
      codeListEvaluator.check(studyMetadataRows, consumer, validationSummary);

      return new EvaluationReport<>(evaluationResults, validationSummary.getValidationResults());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
