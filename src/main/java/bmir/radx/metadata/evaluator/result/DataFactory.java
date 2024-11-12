package bmir.radx.metadata.evaluator.result;

import bmir.radx.metadata.evaluator.EvaluationCriterion;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DataFactory {
  public static List<BasicInfoForWriting> getBasicInfoData(List<EvaluationResult> results){
    List<BasicInfoForWriting> basicInfoData = results.stream()
        .filter(result -> result.getEvaluationCriteria().equals(EvaluationCriterion.BASIC_INFO))
        .map(result -> new BasicInfoForWriting(result.getEvaluationMetric(), result.getContentAsInteger()))
        .collect(Collectors.toList());

    // Make sure the “Total Number of Records” metric is written first.
    List<BasicInfoForWriting> sortedData = new ArrayList<>();
    basicInfoData.stream()
        .filter(info -> "Total Number Of Records".equals(info.metric().getDisplayName()))
        .findFirst()
        .ifPresent(sortedData::add);
    basicInfoData.stream()
        .filter(info -> !"Total Number Of Records".equals(info.metric().getDisplayName()))
        .forEach(sortedData::add);

    return sortedData;
  }

  public static List<CriterionForWriting> getCriterionData(List<EvaluationResult> results){
    return results.stream()
        .filter(result -> result.getEvaluationCriteria() != EvaluationCriterion.BASIC_INFO &&
            result.getEvaluationCriteria() != EvaluationCriterion.COMPLETENESS &&
            result.getEvaluationCriteria() != EvaluationCriterion.VOCABULARIES_DISTRIBUTION)
        .collect(Collectors.groupingBy(EvaluationResult::getEvaluationCriteria))
        .entrySet().stream()
        .map(entry -> {
          EvaluationCriterion criterion = entry.getKey();
          List<EvaluationResult> criterionResults = entry.getValue();
          var criterionForWriting = new CriterionForWriting();
          criterionForWriting.setCriterion(criterion);
          for(var result : criterionResults){
            var metric = result.getEvaluationMetric().getDisplayName();
            if (metric.contains("Rate")){
              criterionForWriting.setPassRate(result.getContentAsDouble());
            } else if (metric.contains("Number")) {
              criterionForWriting.setFailedStudyCount(result.getContentAsInteger());
            } else {
              criterionForWriting.setFailedStudies(result.getContentAsString());
            }
          }
          return criterionForWriting;
        })
        .sorted(Comparator.comparing(criterionForWriting -> criterionForWriting.getCriterion().name()))
        .collect(Collectors.toList());
  }
}
