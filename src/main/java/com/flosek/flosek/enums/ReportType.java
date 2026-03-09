package com.flosek.flosek.enums;

/**
 * Enum for Report Types
 */
public enum ReportType {
    MONTHLY("Monthly Report"),
    YEARLY("Yearly Report"),
    QUARTERLY("Quarterly Report"),
    CUSTOM("Custom Date Range Report"),
    CATEGORY("Category Analysis Report"),
    COMPARISON("Month-over-Month Comparison");

    private final String displayName;

    ReportType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
