package bmir.radx.metadata.evaluator.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.TemplateSchemaArtifact;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class TemplateGetter {
  @Value("${study.template.file.name}")
  private String studyTemplateFileName;

  @Value("${data.file.template.file.name}")
  private String dataFileTemplateFileName;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public TemplateSchemaArtifact getStudyTemplate() {
    return getTemplate(studyTemplateFileName);
  }

  public TemplateSchemaArtifact getDataFileTemplate() {
    return getTemplate(dataFileTemplateFileName);
  }

  public String getDataFileTemplateString(){
    return getTemplateString(dataFileTemplateFileName);
  }

  private TemplateSchemaArtifact getTemplate(String fileName) {
    var jsonSchemaArtifactReader = new JsonSchemaArtifactReader();
    try (InputStream inputStream = TemplateGetter.class.getClassLoader().getResourceAsStream(fileName)) {
      var templateNode = objectMapper.readTree(inputStream);
      return jsonSchemaArtifactReader.readTemplateSchemaArtifact((ObjectNode) templateNode);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String getTemplateString(String fileName){
    try{
      var resourceUrl = TemplateGetter.class.getClassLoader().getResource(fileName);
      if (resourceUrl == null) {
        throw new IllegalArgumentException(fileName + " not found!");
      }
      var path = Paths.get(resourceUrl.toURI());
      return Files.readString(path);
    } catch (IOException | URISyntaxException e) {
      throw new RuntimeException("Error get string for file " + fileName);
    }
  }
}
