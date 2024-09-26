package bmir.radx.metadata.evaluator.study;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class StudyTemplateGetter {
  @Value("${study.template.file.name}")
  private String studyTemplateFileName;

  public TemplateSchemaArtifact getTemplate() {
    var objectMapper = new ObjectMapper();
    var jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    try (InputStream inputStream = StudyTemplateGetter.class.getClassLoader().getResourceAsStream(studyTemplateFileName)) {
      var templateNode = objectMapper.readTree(inputStream);
      return jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
