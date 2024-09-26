package bmir.radx.metadata.evaluator.util;

import bmir.radx.metadata.evaluator.EvaluationConstant;
import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.JsonValidationResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static bmir.radx.metadata.evaluator.EvaluationConstant.*;
import static bmir.radx.metadata.evaluator.util.DistributionContainer.*;
import static bmir.radx.metadata.evaluator.util.StringParser.parseToStringMap;

@Component
public class ReportAggregator {
  private final Map<Integer, Integer> overallCompleteness = initiateIntMap();
  private final Map<Integer, Integer> requiredCompleteness = initiateIntMap();
  private final Map<Integer, Integer> recommendedCompleteness = initiateIntMap();
  private final Map<Integer, Integer> optionalCompleteness = initiateIntMap();
  private final Map<Integer, Integer> urlsDistribution = initiateIntMap();
  private final Map<String, Integer> ctDistribution = initiateStringMap();
  private final List<JsonValidationResult> validationErrors = new ArrayList<>();

  // Method to aggregate results from each file
  public void aggregate(EvaluationReport<JsonValidationResult> singleReport) {
    var overallCompletion = getIntResult(singleReport, TOTAL_FILLED_FIELDS);
    updateDistribution(overallCompletion, overallCompleteness);

    var requiredCompletionRate = getIntResult(singleReport, FILLED_REQUIRED_FIELDS_COUNT);
    updateDistribution(requiredCompletionRate, requiredCompleteness);

    var recommendedCompletionRate = getIntResult(singleReport, FILLED_RECOMMENDED_FIELDS_COUNT);
    updateDistribution(recommendedCompletionRate, recommendedCompleteness);

    var optionalCompletionRate = getIntResult(singleReport, FILLED_OPTIONAL_FIELDS_COUNT);
    updateDistribution(optionalCompletionRate, optionalCompleteness);

    var urlsCount = getIntResult(singleReport, ACCESSIBLE_URI_COUNT);
    updateDistribution(urlsCount, urlsDistribution);

    var ctMap = getMapResult(singleReport, CONTROLLED_TERMS_FREQUENCY);
    updateDistribution(ctMap, ctDistribution);

    validationErrors.addAll(singleReport.validationResults());
  }

  // Add the summary results for overall completeness and distribution
  public void addSummaryResults(Consumer<EvaluationResult> consumer) {
    //TODO: replace hard coded numbers
    consumer.accept(new EvaluationResult(TOTAL_NUMBER_OF_DATA_FILES, "200"));
    consumer.accept(new EvaluationResult(TOTAL_FIELDS, "106"));
    consumer.accept(new EvaluationResult(OVERALL_COMPLETENESS_DISTRIBUTION, overallCompleteness.toString()));
    consumer.accept(new EvaluationResult(TOTAL_REQUIRED_FIELDS, "2"));
    consumer.accept(new EvaluationResult(REQUIRED_FIELDS_COMPLETENESS_DISTRIBUTION, requiredCompleteness.toString()));
    consumer.accept(new EvaluationResult(TOTAL_RECOMMENDED_FIELDS, "20"));
    consumer.accept(new EvaluationResult(RECOMMENDED_FIELDS_COMPLETENESS_DISTRIBUTION, recommendedCompleteness.toString()));
    consumer.accept(new EvaluationResult(TOTAL_OPTIONAL_FIELDS, "84"));
    consumer.accept(new EvaluationResult(OPTIONAL_FIELDS_COMPLETENESS_DISTRIBUTION, optionalCompleteness.toString()));
    consumer.accept(new EvaluationResult(URL_COUNT_DISTRIBUTION, urlsDistribution.toString()));
    consumer.accept(new EvaluationResult(CONTROLLED_TERMS_DISTRIBUTION, ctDistribution.toString()));
    consumer.accept(new EvaluationResult(ERRORS_NUMBER, String.valueOf(validationErrors.size())));
  }

  private Integer getIntResult(EvaluationReport<JsonValidationResult> report, EvaluationConstant evaluationConstant) {
    for (var result : report.evaluationResults()) {
      if (evaluationConstant.equals(result.getEvaluationConstant())) {
        return Integer.valueOf(result.getContent());
      }
    }
    throw new IllegalArgumentException(evaluationConstant.getDisplayName() + " not found in the report.");
  }

  private Map<String, Integer> getMapResult(EvaluationReport<JsonValidationResult> report, EvaluationConstant evaluationConstant) {
    for (var result : report.evaluationResults()) {
      if (evaluationConstant.equals(result.getEvaluationConstant())) {
        return parseToStringMap(result.getContent());
      }
    }
    throw new IllegalArgumentException(evaluationConstant.getDisplayName() + " not found in the report.");
  }

  public List<JsonValidationResult> getValidationErrors() {
    return validationErrors;
  }
}
