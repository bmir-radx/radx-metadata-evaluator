package bmir.radx.metadata.evaluator.statistics;

public class CompletenessStatistics {
    private final int requiredFields;
    private final int filledRequiredFields;
    private final double filledRequiredPercentage;
    private final int recommendedFields;
    private final int filledRecommendedFields;
    private final double filledRecommendedPercentage;
    private final int optionalFields;
    private final int filledOptionalFields;
    private final double filledOptionalPercentage;
    private final int totalFields;
    private final int filledTotalFields;
    private final double filledTotalPercentage;

    private CompletenessStatistics(Builder builder) {
        this.requiredFields = builder.requiredFields;
        this.filledRequiredFields = builder.filledRequiredFields;
        this.filledRequiredPercentage = builder.filledRequiredPercentage;
        this.recommendedFields = builder.recommendedFields;
        this.filledRecommendedFields = builder.filledRecommendedFields;
        this.filledRecommendedPercentage = builder.filledRecommendedPercentage;
        this.optionalFields = builder.optionalFields;
        this.filledOptionalFields = builder.filledOptionalFields;
        this.filledOptionalPercentage = builder.filledOptionalPercentage;
        this.totalFields = builder.totalFields;
        this.filledTotalFields = builder.filledTotalFields;
        this.filledTotalPercentage = builder.filledTotalPercentage;
    }

    public static CompletenessStatistics create(Builder builder) {
        return new CompletenessStatistics(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int requiredFields;
        private int filledRequiredFields;
        private double filledRequiredPercentage;
        private int recommendedFields;
        private int filledRecommendedFields;
        private double filledRecommendedPercentage;
        private int optionalFields;
        private int filledOptionalFields;
        private double filledOptionalPercentage;
        private int totalFields;
        private int filledTotalFields;
        private double filledTotalPercentage;

        public Builder requiredFields(int requiredFields) {
            this.requiredFields = requiredFields;
            return this;
        }

        public Builder filledRequiredFields(int filledRequiredFields) {
            this.filledRequiredFields = filledRequiredFields;
            return this;
        }

        public Builder filledRequiredPercentage(double filledRequiredPercentage) {
            this.filledRequiredPercentage = filledRequiredPercentage;
            return this;
        }

        public Builder recommendedFields(int recommendedFields) {
            this.recommendedFields = recommendedFields;
            return this;
        }

        public Builder filledRecommendedFields(int filledRecommendedFields) {
            this.filledRecommendedFields = filledRecommendedFields;
            return this;
        }

        public Builder filledRecommendedPercentage(double filledRecommendedPercentage) {
            this.filledRecommendedPercentage = filledRecommendedPercentage;
            return this;
        }

        public Builder optionalFields(int optionalFields) {
            this.optionalFields = optionalFields;
            return this;
        }

        public Builder filledOptionalFields(int filledOptionalFields) {
            this.filledOptionalFields = filledOptionalFields;
            return this;
        }

        public Builder filledOptionalPercentage(double filledOptionalPercentage) {
            this.filledOptionalPercentage = filledOptionalPercentage;
            return this;
        }

        public Builder totalFields(int totalFields) {
            this.totalFields = totalFields;
            return this;
        }

        public Builder filledTotalFields(int filledTotalFields) {
            this.filledTotalFields = filledTotalFields;
            return this;
        }

        public Builder filledTotalPercentage(double filledTotalPercentage) {
            this.filledTotalPercentage = filledTotalPercentage;
            return this;
        }

        public CompletenessStatistics build() {
            return CompletenessStatistics.create(this);
        }
    }

    public int getRequiredFields() {
        return requiredFields;
    }

    public int getFilledRequiredFields() {
        return filledRequiredFields;
    }

    public double getFilledRequiredPercentage() {
        return filledRequiredPercentage;
    }

    public int getRecommendedFields() {
        return recommendedFields;
    }

    public int getFilledRecommendedFields() {
        return filledRecommendedFields;
    }

    public double getFilledRecommendedPercentage() {
        return filledRecommendedPercentage;
    }

    public int getOptionalFields() {
        return optionalFields;
    }

    public int getFilledOptionalFields() {
        return filledOptionalFields;
    }

    public double getFilledOptionalPercentage() {
        return filledOptionalPercentage;
    }

    public int getTotalFields() {
        return totalFields;
    }

    public int getFilledTotalFields() {
        return filledTotalFields;
    }

    public double getFilledTotalPercentage() {
        return filledTotalPercentage;
    }
}
