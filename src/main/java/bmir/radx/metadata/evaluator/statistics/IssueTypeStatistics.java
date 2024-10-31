package bmir.radx.metadata.evaluator.statistics;

import bmir.radx.metadata.evaluator.util.IssueTypeMapping;

public class IssueTypeStatistics {
    private final IssueTypeMapping.IssueType issueType;
    private final int count;
    private final double percentage;

    private IssueTypeStatistics(Builder builder) {
        this.issueType = builder.issueType;
        this.count = builder.count;
        this.percentage = builder.percentage;
    }

    public static IssueTypeStatistics create(Builder builder) {
        return new IssueTypeStatistics(builder);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private IssueTypeMapping.IssueType issueType;
        private int count;
        private double percentage;

        public Builder issueType(IssueTypeMapping.IssueType issueType) {
            this.issueType = issueType;
            return this;
        }

        public Builder count(int count) {
            this.count = count;
            return this;
        }

        public Builder percentage(double percentage) {
            this.percentage = percentage;
            return this;
        }

        public IssueTypeStatistics build() {
            return IssueTypeStatistics.create(this);
        }
    }

    public IssueTypeMapping.IssueType getIssueType() {
        return issueType;
    }

    public int getCount() {
        return count;
    }

    public double getPercentage() {
        return percentage;
    }
}
