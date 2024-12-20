package bmir.radx.metadata.evaluator.dataFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.reader.JsonArtifactReader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@Component
public class DataFileMetadataReader {
  private final ObjectMapper mapper;

  public DataFileMetadataReader(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public Map<Path, TemplateInstanceArtifact> readDataFileMetadata(Path filePath) {
    Map<Path, TemplateInstanceArtifact> artifacts = new HashMap<>();
    if (Files.isDirectory(filePath)) {
      try {
        Files.walkFileTree(filePath, new SimpleFileVisitor<>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (file.toString().endsWith(".json")) {
              var artifact = processSingleFile(file);
              if (artifact != null) {
                artifacts.put(file, artifact);
              }
            }
            return FileVisitResult.CONTINUE;
          }

          @Override
          public FileVisitResult visitFileFailed(Path file, IOException exc) {
            System.err.println("Failed to access file: " + file + " - " + exc.getMessage());
            return FileVisitResult.CONTINUE;
          }
        });
      } catch (IOException e) {
        System.err.println("Error processing files in directory " + filePath + ": " + e.getMessage());
      }
    } else {
      var artifact = processSingleFile(filePath);
      if (artifact != null) {
        artifacts.put(filePath, artifact);
      }
    }
    return artifacts;
  }

  private TemplateInstanceArtifact processSingleFile(Path filePath){
    try {
      var instanceNode = mapper.readTree(Files.readString(filePath));
      var jsonSchemaArtifactReader = new JsonArtifactReader();
      return jsonSchemaArtifactReader.readTemplateInstanceArtifact((ObjectNode) instanceNode);
    } catch (IOException e) {
      throw new RuntimeException("Error read file " + filePath  + ": " + e.getMessage());
    }
  }
}
