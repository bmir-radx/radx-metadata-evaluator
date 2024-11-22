package bmir.radx.metadata.evaluator.result;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ValidationSummary<T extends ValidationResult> {
  private List<T> validationResults;
  private Set<String> invalidMetadata;

  public ValidationSummary(List<T> validationResults, Set<String> invalidMetadata) {
    this.validationResults = new ArrayList<>(validationResults);
    this.invalidMetadata = invalidMetadata;
  }

  public List<T> getValidationResults() {
    return validationResults;
  }

  public void setValidationResults(List<T> validationResults) {
    this.validationResults = new ArrayList<>(validationResults);
  }

  public Set<String> getInvalidMetadata() {
    return invalidMetadata;
  }

  public void setInvalidMetadata(Set<String> invalidMetadata) {
    this.invalidMetadata = invalidMetadata;
  }

  public void addInvalidMetadata(String metadata) {
    this.invalidMetadata.add(metadata);
  }
  public void addMultiInvalidMetadata(List<String> metadataList){
    this.invalidMetadata.addAll(metadataList);
  }

  public void updateValidationResults(List<T> newValidationResults) {
    this.validationResults.addAll(newValidationResults);
  }

  public void updateValidationResult(T newValidationResult){
    this.validationResults.add(newValidationResult);
  }
}
