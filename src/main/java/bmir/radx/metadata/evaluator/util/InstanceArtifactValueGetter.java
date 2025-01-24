package bmir.radx.metadata.evaluator.util;

import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;

import static bmir.radx.metadata.evaluator.util.InstanceArtifactPath.*;

public class InstanceArtifactValueGetter {
  public static String getValue(TemplateInstanceArtifact instanceArtifact, InstanceArtifactPath path){
    var valuesReporter = new TemplateInstanceValuesReporter(instanceArtifact);
    var fieldArtifact = valuesReporter.getValues().get(path.getPath());
    return fieldArtifact.jsonLdValue().orElse(null);
  }

  public static String getStudyName(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, STUDY_NAME_PATH);
  }

  public static String getStudyPhs(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, STUDY_PHS_PATH);
  }

  public static String getTitle(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, TITLE_PATH);
  }

  public static String getFileName(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, FILE_NAME_PATH);
  }

  public static String getAwardLocalIdentifier(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, AWARD_LOCAL_IDENTIFIER_PATH);
  }

  public static String getDataCharSummary(TemplateInstanceArtifact instanceArtifact){
    return getValue(instanceArtifact, DATA_CHAR_SUMMARY_PATH);
  }
}
