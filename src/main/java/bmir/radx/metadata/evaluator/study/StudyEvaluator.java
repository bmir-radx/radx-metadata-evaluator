package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.SpreadsheetReader;
import bmir.radx.metadata.evaluator.result.SpreadsheetValidationResult;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;

@Component
public class StudyEvaluator {
  private final StudyCompletenessEvaluator completenessEvaluator;
  private final StudyValidityEvaluator studyValidityEvaluator;
  private final StudyLinkEvaluator studyLinkEvaluator;
  private final ClinicalTrialsChecker clinicalTrialsChecker;

  public StudyEvaluator(StudyCompletenessEvaluator completenessEvaluator,
                        StudyValidityEvaluator studyValidityEvaluator,
                        StudyLinkEvaluator studyLinkEvaluator, ClinicalTrialsChecker clinicalTrialsChecker) {
    this.completenessEvaluator = completenessEvaluator;
    this.studyValidityEvaluator = studyValidityEvaluator;
    this.studyLinkEvaluator = studyLinkEvaluator;
    this.clinicalTrialsChecker = clinicalTrialsChecker;
  }

  public EvaluationReport<SpreadsheetValidationResult> evaluate(Path metadataFilePath) {
    var evaluationResults = new ArrayList<EvaluationResult>();
    Consumer<EvaluationResult> consumer = evaluationResults::add;

    var studyMetadataReader = new SpreadsheetReader();
    try {
      var studyMetadataRows = studyMetadataReader.readStudyMetadata(metadataFilePath);

//      var descriptionExplorer = new DescriptionExplorer();
//      descriptionExplorer.processMetadata(studyMetadataRows, "StudyDescriptions.xlsx");
//      return null;

      completenessEvaluator.evaluate(studyMetadataRows, consumer);
      System.out.println("Start to validate spreadsheet");
      var validationResults = studyValidityEvaluator.evaluate(metadataFilePath, consumer);
      studyLinkEvaluator.evaluate(studyMetadataRows, consumer);
      System.out.println("Start to check clinicalTrials link");
      clinicalTrialsChecker.checkClinicalTrialsContent(studyMetadataRows, consumer, validationResults);
      return new EvaluationReport<>(evaluationResults, validationResults);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
