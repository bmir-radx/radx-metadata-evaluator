package bmir.radx.metadata.evaluator.util;

import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;

public class InstanceArtifactValueGetter {
  private static final String studyNamePath = "/Data File Parent Studies[0]/Study Name";
  private static final String studyPhsPath = "/Data File Parent Studies[0]/PHS Identifier";
  private static final String titlePath = "/Data File Titles[0]/Title";
  private static final String fileNamePath = "/Data File Identity/File Name";
  private static final String awardLocalIdentifierPath = "/Data File Funding Sources[0]/Award Local Identifier";

  public static String getValue(TemplateInstanceArtifact instanceArtifact, String fieldPath){
    var valuesReporter = new TemplateInstanceValuesReporter(instanceArtifact);
    var fieldArtifact = valuesReporter.getValues().get(fieldPath);
    return fieldArtifact.jsonLdValue().orElse(null);
  }

  public static String getStudyName(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, studyNamePath);
  }

  public static String getStudyPhs(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, studyPhsPath);
  }

  public static String getTitle(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, titlePath);
  }

  public static String getFileName(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, fileNamePath);
  }

  public static String getAwardLocalIdentifier(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, awardLocalIdentifierPath);
  }
}
