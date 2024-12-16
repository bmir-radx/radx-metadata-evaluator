package bmir.radx.metadata.evaluator;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;
import org.metadatacenter.artifacts.model.renderer.JsonArtifactRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public JsonArtifactRenderer jsonSchemaArtifactRenderer() {
    return new JsonArtifactRenderer();
  }

  @Bean
  public JsonArtifactReader jsonSchemaArtifactReader(){
    return new JsonArtifactReader();
  }
}
