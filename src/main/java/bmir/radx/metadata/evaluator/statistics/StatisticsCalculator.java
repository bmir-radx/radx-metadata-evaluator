package bmir.radx.metadata.evaluator.statistics;

import bmir.radx.metadata.evaluator.EvaluationMetric;
import bmir.radx.metadata.evaluator.EvaluationReport;
import bmir.radx.metadata.evaluator.result.EvaluationResult;
import bmir.radx.metadata.evaluator.result.ValidationResult;
import bmir.radx.metadata.evaluator.util.IssueTypeMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static bmir.radx.metadata.evaluator.EvaluationMetric.*;

public class StatisticsCalculator {
    public static RecordStatistics calculateRecordStats(EvaluationReport<?> evaluationReport){
        int totalRecords = 0;
        int invalidRecords = 0;

        for (EvaluationResult result : evaluationReport.evaluationResults()) {
            if (TOTAL_NUMBER_OF_RECORDS.equals(result.getEvaluationMetric())) {
                totalRecords = result.getContentAsInteger();
            } else if (NUMBER_OF_INVALID_RECORDS.equals(result.getEvaluationMetric())) {
                invalidRecords = result.getContentAsInteger();
            }
        }

        int validRecords = totalRecords - invalidRecords;
        double invalidPercentage = totalRecords > 0 ? Math.round((invalidRecords * 100.0 / totalRecords) * 100.0) / 100.0 : 0.0;
        double validPercentage = totalRecords > 0 ? Math.round((validRecords * 100.0 / totalRecords) * 100.0) / 100.0 : 0.0;

        return RecordStatistics.builder()
                .totalRecords(totalRecords)
                .invalidRecords(invalidRecords)
                .validRecords(totalRecords)
                .invalidPercentage(invalidPercentage)
                .validPercentage(validPercentage)
                .build();
    }

    public static CompletenessStatistics calculateCompletenessStatistics(EvaluationReport<?> evaluationReport) {
        int totalRecords = 0;
        int totalFields = 0;
        int filledTotalFields = 0;
        int totalRequiredFields = 0;
        int filledRequiredFields = 0;
        int totalRecommendedFields = 0;
        int filledRecommendedFields = 0;
        int totalOptionalFields = 0;
        int filledOptionalFields = 0;

        for (EvaluationResult result : evaluationReport.evaluationResults()) {
            if (TOTAL_NUMBER_OF_RECORDS.equals(result.getEvaluationMetric())) {
                totalRecords = result.getContentAsInteger();
            } else if (TOTAL_REQUIRED_FIELDS.equals(result.getEvaluationMetric())) {
                totalRequiredFields = result.getContentAsInteger();
            } else if (TOTAL_RECOMMENDED_FIELDS.equals(result.getEvaluationMetric())) {
                totalRecommendedFields = result.getContentAsInteger();
            } else if (TOTAL_OPTIONAL_FIELDS.equals(result.getEvaluationMetric())) {
                totalOptionalFields = result.getContentAsInteger();
            } else if (TOTAL_FIELDS.equals(result.getEvaluationMetric())) {
                totalFields = result.getContentAsInteger();
            } else if (REQUIRED_FIELDS_COMPLETENESS_DISTRIBUTION.equals(result.getEvaluationMetric())) {
                Map<Integer, Integer> distribution = result.getContentAsMapIntegerInteger();
                for (Map.Entry<Integer, Integer> entry : distribution.entrySet()) {
                    filledRequiredFields += entry.getKey() * entry.getValue();
                }
            } else if (RECOMMENDED_FIELDS_COMPLETENESS_DISTRIBUTION.equals(result.getEvaluationMetric())) {
                Map<Integer, Integer> distribution = result.getContentAsMapIntegerInteger();
                for (Map.Entry<Integer, Integer> entry : distribution.entrySet()) {
                    filledRecommendedFields += entry.getKey() * entry.getValue();
                }
            } else if (OPTIONAL_FIELDS_COMPLETENESS_DISTRIBUTION.equals(result.getEvaluationMetric())) {
                Map<Integer, Integer> distribution = result.getContentAsMapIntegerInteger();
                for (Map.Entry<Integer, Integer> entry : distribution.entrySet()) {
                    filledOptionalFields += entry.getKey() * entry.getValue();
                }
            } else if (OVERALL_COMPLETENESS_DISTRIBUTION.equals(result.getEvaluationMetric())) {
                Map<Integer, Integer> distribution = result.getContentAsMapIntegerInteger();
                for (Map.Entry<Integer, Integer> entry : distribution.entrySet()) {
                    filledTotalFields += entry.getKey() * entry.getValue();
                }
            }
        }

        totalFields *= totalRecords;
        totalRequiredFields *= totalRecords;
        totalRecommendedFields *= totalRecords;
        totalOptionalFields *= totalRecords;

        double filledRequiredPercentage = totalRequiredFields > 0 ? Math.round((filledRequiredFields * 100.0 / totalRequiredFields) * 100.0) / 100.0 : 0.0;
        double filledRecommendedPercentage = totalRecommendedFields > 0 ? Math.round((filledRecommendedFields * 100.0 / totalRecommendedFields) * 100.0) / 100.0 : 0.0;
        double filledOptionalPercentage = totalOptionalFields > 0 ? Math.round((filledOptionalFields * 100.0 / totalOptionalFields) * 100.0) / 100.0 : 0.0;
        double filledTotalPercentage = totalFields > 0 ? Math.round((filledTotalFields * 100.0 / totalFields) * 100.0) / 100.0 : 0.0;

        return CompletenessStatistics.builder()
                .requiredFields(totalRequiredFields)
                .filledRequiredFields(filledRequiredFields)
                .filledRequiredPercentage(filledRequiredPercentage)
                .recommendedFields(totalRecommendedFields)
                .filledRecommendedFields(filledRecommendedFields)
                .filledRecommendedPercentage(filledRecommendedPercentage)
                .optionalFields(totalOptionalFields)
                .filledOptionalFields(filledOptionalFields)
                .filledOptionalPercentage(filledOptionalPercentage)
                .totalFields(totalFields)
                .filledTotalFields(filledTotalFields)
                .filledTotalPercentage(filledTotalPercentage)
                .build();
    }

    public static List<IssueTypeStatistics> calculateIssueTypeStatistics(EvaluationReport<?> evaluationReport) {
        Map<IssueTypeMapping.IssueType, Integer> issueTypeCounts = new HashMap<>();
        int totalValidationResults = evaluationReport.validationResults().size();

        for (var validationResult : evaluationReport.validationResults()) {
            var issueType = validationResult.issueType();
            issueTypeCounts.put(issueType, issueTypeCounts.getOrDefault(issueType, 0) + 1);
        }

        List<IssueTypeStatistics> results = new ArrayList<>();
        for (Map.Entry<IssueTypeMapping.IssueType, Integer> entry : issueTypeCounts.entrySet()) {
            var issueType = entry.getKey();
            int count = entry.getValue();
            double percentage = totalValidationResults > 0 ? Math.round((count * 100.0 / totalValidationResults) * 100.0) / 100.0 : 0.0;
            var stats = IssueTypeStatistics.builder()
                    .issueType(issueType)
                    .count(count)
                    .percentage(percentage)
                    .build();
            results.add(stats);
        }

        return results;
    }
}
