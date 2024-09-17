package bmir.radx.metadata.evaluator.study;

import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.EvaluationResult;
import bmir.radx.metadata.evaluator.SpreadsheetReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.function.Consumer;

@Component
public class StudyEvaluator {
  private final StudyCompletenessEvaluator completenessEvaluator;
  private final StudyValidityEvaluator studyValidityEvaluator;

  public StudyEvaluator(StudyCompletenessEvaluator completenessEvaluator, StudyValidityEvaluator studyValidityEvaluator) {
    this.completenessEvaluator = completenessEvaluator;
    this.studyValidityEvaluator = studyValidityEvaluator;
  }

  public EvaluationReport evaluate(Path metadataFilePath) {
    var results = new ArrayList<EvaluationResult>();
    Consumer<EvaluationResult> consumer = results::add;

//    studyValidityEvaluator.evaluate(metadataFilePath, consumer);
    var studyMetadataReader = new SpreadsheetReader();
    try {
      var studyMetadataRows = studyMetadataReader.readStudyMetadata(metadataFilePath);
      completenessEvaluator.evaluate(studyMetadataRows, consumer);

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return new EvaluationReport(results);
  }
}
