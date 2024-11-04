package bmir.radx.metadata.evaluator.statistics;

import bmir.radx.metadata.evaluator.util.FieldCategory;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static bmir.radx.metadata.evaluator.util.FieldCategory.*;

public class ChartDataFactory {
    public static Map<String, Integer> getValidityDistribution(RecordStatistics recordStatistics) {
        Map<String, Integer> validityDistribution = new LinkedHashMap<>();
        validityDistribution.put("Valid Records", recordStatistics.getValidRecords());
        validityDistribution.put("Invalid Records", recordStatistics.getInvalidRecords());
        return validityDistribution;
    }

    public static Map<String, Integer> getIssueTypeDistribution(List<IssueTypeStatistics> issueTypeStatistics){
        Map<String, Integer> validityDistribution = new LinkedHashMap<>();
        for(var statistic : issueTypeStatistics){
            validityDistribution.put(statistic.getIssueType().name(), statistic.getCount());
        }
        return validityDistribution;
    }

    public static Map<String, Map<String, Integer>> getCompletenessDistribution(CompletenessStatistics completenessStatistics){
        String unfilled = "unfilled";
        String filled = "filled";
        Map<String, Map<String, Integer>> distribution = new LinkedHashMap<>();
        Map<String, Integer> required = new HashMap<>();
        Map<String, Integer> recommended = new HashMap<>();
        Map<String, Integer> optional = new HashMap<>();
        Map<String, Integer> overall = new HashMap<>();

        required.put(unfilled, completenessStatistics.getRequiredFields() - completenessStatistics.getFilledRequiredFields());
        required.put(filled, completenessStatistics.getFilledRequiredFields());
        distribution.put(REQUIRED.getCategory(), required);

        recommended.put(unfilled, completenessStatistics.getRecommendedFields() - completenessStatistics.getFilledRecommendedFields());
        recommended.put(filled, completenessStatistics.getFilledRecommendedFields());
        distribution.put(RECOMMENDED.getCategory(), recommended);

        optional.put(unfilled, completenessStatistics.getOptionalFields() - completenessStatistics.getFilledOptionalFields());
        optional.put(filled, completenessStatistics.getFilledOptionalFields());
        distribution.put(OPTIONAL.getCategory(), optional);

        overall.put(unfilled, completenessStatistics.getTotalFields() - completenessStatistics.getFilledTotalFields());
        overall.put(filled, completenessStatistics.getFilledTotalFields());
        distribution.put(OVERALL.getCategory(), overall);

        return distribution;
    }
}
