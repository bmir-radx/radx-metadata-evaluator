package bmir.radx.metadata.evaluator.sharedComponents;

import java.util.Map;

public record CompletionResult(Map<FieldRequirement, Double> completionRates,
                               int totalRequiredFields,
                               int totalRecommendedFields,
                               int totalOptionalFields,
                               int totalFields,
                               int filledRequiredFields,
                               int filledRecommendedFields,
                               int filledOptionalFields,
                               int totalFilledFields) {
}
