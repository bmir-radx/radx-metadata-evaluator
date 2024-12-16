package bmir.radx.metadata.evaluator.sharedComponents;

import bmir.radx.metadata.evaluator.dataFile.FieldsCollector;
import bmir.radx.metadata.evaluator.study.StudyMetadataRow;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CompletionRateCheckerTest {
  private ObjectMapper objectMapper;
  private FieldsCollector fieldsCollector;
  private JsonArtifactReader jsonSchemaArtifactReader;
  private CompletionRateChecker completionRateChecker;
  private final String fileName = "StudyTemplate.json";

  @BeforeEach
  void setUp(){
    objectMapper = new ObjectMapper();
    fieldsCollector = new FieldsCollector();
    jsonSchemaArtifactReader = new JsonArtifactReader();
    completionRateChecker = new CompletionRateChecker(fieldsCollector);
  }

  @Test
  void test(){
    try (InputStream inputStream = CompletionRateCheckerTest.class.getClassLoader().getResourceAsStream(fileName)) {
      var templateNode = objectMapper.readTree(inputStream);
      var templateSchemaArtifact = jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
      var studyMetadataInstance = new StudyMetadataRow(1, "RADx Up", null, null, null, null,
          null, null, null, null, null, null, null, null,
          null, null, null, null, null, null, null, null, null, null,
          null, null, null, null, null, null, null, null, null);
      var report = completionRateChecker.getSpreadsheetRowCompleteness(studyMetadataInstance, templateSchemaArtifact);
      assertNotNull(report, "Report should not be null");
    } catch (IOException e) {
      throw new RuntimeException("Error reading json file");
    }
  }
}
