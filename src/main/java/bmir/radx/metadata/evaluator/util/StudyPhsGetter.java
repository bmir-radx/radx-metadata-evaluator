package bmir.radx.metadata.evaluator.util;

import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;

public class StudyPhsGetter {
  private static final String DATA_FILE_PARENT_STUDIES = "Data File Parent Studies";
  private static final String PHS_IDENTIFIER = "PHS Identifier";
  public static String getStudyPhs(TemplateInstanceArtifact templateInstanceArtifact){
    var parentStudiesArtifacts = templateInstanceArtifact.multiInstanceElementInstances().get(DATA_FILE_PARENT_STUDIES);
    var dataFileStudyPhs = parentStudiesArtifacts.get(0).singleInstanceFieldInstances().get(PHS_IDENTIFIER).jsonLdValue();
    return dataFileStudyPhs.orElse(null);
  }
}
