package bmir.radx.metadata.evaluator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.metadatacenter.artifacts.model.reader.JsonSchemaArtifactReader;
import org.metadatacenter.artifacts.model.renderer.JsonSchemaArtifactRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public JsonSchemaArtifactRenderer jsonSchemaArtifactRenderer() {
    return new JsonSchemaArtifactRenderer();
  }

  @Bean
  public JsonSchemaArtifactReader jsonSchemaArtifactReader(){
    return new JsonSchemaArtifactReader();
  }
}
