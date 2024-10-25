package bmir.radx.metadata.evaluator.util;

import edu.stanford.bmir.radx.metadata.validator.lib.TemplateInstanceValuesReporter;
import org.metadatacenter.artifacts.model.core.TemplateInstanceArtifact;
import org.metadatacenter.artifacts.model.visitors.TemplateReporter;
import org.springframework.stereotype.Component;

@Component
public class ReporterGetter {
  private final TemplateGetter templateGetter;

  public ReporterGetter(TemplateGetter templateGetter) {
    this.templateGetter = templateGetter;
  }

  public TemplateReporter getTemplateReporter(){
    var template = templateGetter.getDataFileTemplate();
    return new TemplateReporter(template);
  }

  public TemplateInstanceValuesReporter getTemplateInstanceValuesReporter(TemplateInstanceArtifact instance){
    return new TemplateInstanceValuesReporter(instance);
  }
}
