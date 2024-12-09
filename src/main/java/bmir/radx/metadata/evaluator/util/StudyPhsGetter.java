package bmir.radx.metadata.evaluator.util;

import bmir.radx.metadata.evaluator.SpreadsheetReader;
import bmir.radx.metadata.evaluator.dataFile.DataFileMetadataReader;
import bmir.radx.metadata.evaluator.study.StudyMetadataRow;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

@Component
public class StudyPhsGetter {
  private final String DATA_FILE_PARENT_STUDIES = "Data File Parent Studies";
  private final String PHS_IDENTIFIER = "PHS Identifier";
  private final DataFileMetadataReader dataFileMetadataReader;
  private final SpreadsheetReader studyMetadataReader;

  public StudyPhsGetter(DataFileMetadataReader dataFileMetadataReader, SpreadsheetReader studyMetadataReader) {
    this.dataFileMetadataReader = dataFileMetadataReader;
    this.studyMetadataReader = studyMetadataReader;
  }

  public String getCleanStudyPhs(TemplateInstanceArtifact templateInstanceArtifact){
    String phs = getStudyPhs(templateInstanceArtifact).trim();
    String pattern = "(phs\\d{6})"; // Regex pattern to match "phs" followed by 6 digits
    java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
    java.util.regex.Matcher matcher = regex.matcher(phs);

    if (matcher.find()) {
      return matcher.group(1);
    }

    return null;
  }

  public String getStudyPhs(TemplateInstanceArtifact templateInstanceArtifact){
    var parentStudiesArtifacts = templateInstanceArtifact.multiInstanceElementInstances().get(DATA_FILE_PARENT_STUDIES);
    var dataFileStudyPhs = parentStudiesArtifacts.get(0).singleInstanceFieldInstances().get(PHS_IDENTIFIER).jsonLdValue();
    return dataFileStudyPhs.orElse(null);
  }

  public Set<String> getStudyPhsPool(Path study, Path datafile) throws IOException {
    Set<String> studyPhsPool = new HashSet<>();
    Collection<TemplateInstanceArtifact> dataFileInstances = new ArrayList<>();
    List<StudyMetadataRow> studyMetadataInstances = new ArrayList<>();

    if(study != null){
      studyMetadataInstances = studyMetadataReader.readStudyMetadata(study);
      updatePool(studyPhsPool, studyMetadataInstances);
    }
    if(datafile != null){
      dataFileInstances = dataFileMetadataReader.readDataFileMetadata(datafile).values();
      updatePool(studyPhsPool, dataFileInstances);
    }

    return studyPhsPool;
  }

  private void updatePool(Set<String> studyPhsPool, Collection<TemplateInstanceArtifact> dataFileInstances){
    for(var dataFileInstance: dataFileInstances){
      var studyPhs = getCleanStudyPhs(dataFileInstance);
      studyPhsPool.add(studyPhs);
    }
  }

  private void updatePool(Set<String> studyPhsPool, List<StudyMetadataRow> studyMetadataInstances){
    for(var studyMetadataInstance: studyMetadataInstances){
      studyPhsPool.add(studyMetadataInstance.studyPHS());
    }
  }
}
