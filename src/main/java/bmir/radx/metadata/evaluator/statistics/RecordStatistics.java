package bmir.radx.metadata.evaluator.statistics;

public class RecordStatistics {
    private final int totalRecords;
    private final int invalidRecords;
    private final int validRecords;
    private final double invalidPercentage;
    private final double validPercentage;

    private RecordStatistics(Builder builder) {
        this.totalRecords = builder.totalRecords;
        this.invalidRecords = builder.invalidRecords;
        this.validRecords = builder.validRecords;
        this.invalidPercentage = builder.invalidPercentage;
        this.validPercentage = builder.validPercentage;
    }

    public static RecordStatistics create(Builder builder) {
        return new RecordStatistics(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int totalRecords;
        private int invalidRecords;
        private int validRecords;
        private double invalidPercentage;
        private double validPercentage;

        public Builder totalRecords(int totalRecords) {
            this.totalRecords = totalRecords;
            return this;
        }

        public Builder invalidRecords(int invalidRecords) {
            this.invalidRecords = invalidRecords;
            return this;
        }

        public Builder validRecords(int validRecords) {
            this.validRecords = validRecords;
            return this;
        }

        public Builder invalidPercentage(double invalidPercentage) {
            this.invalidPercentage = invalidPercentage;
            return this;
        }

        public Builder validPercentage(double validPercentage) {
            this.validPercentage = validPercentage;
            return this;
        }

        public RecordStatistics build() {
            return RecordStatistics.create(this);
        }
    }


    public int getTotalRecords() {
        return totalRecords;
    }

    public int getInvalidRecords() {
        return invalidRecords;
    }

    public int getValidRecords() {
        return validRecords;
    }

    public double getInvalidPercentage() {
        return invalidPercentage;
    }

    public double getValidPercentage() {
        return validPercentage;
    }
}
