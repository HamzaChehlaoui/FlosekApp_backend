package com.flosek.flosek.dto.response;

import com.flosek.flosek.enums.ReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for reports data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO {

    // Report metadata
    private ReportType reportType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodLabel;

    // Financial Summary
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netSavings;
    private BigDecimal savingsRate;

    // Changes from previous period
    private BigDecimal incomeChange;
    private BigDecimal expenseChange;
    private BigDecimal savingsChange;

    // Monthly data for trends
    private List<MonthlyReportData> monthlyData;

    // Category breakdown
    private List<CategoryReportData> categoryBreakdown;

    // Top expenses
    private List<TopExpenseData> topExpenses;

    /**
     * Monthly data point for trend charts
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyReportData {
        private String month;
        private Integer year;
        private BigDecimal income;
        private BigDecimal expenses;
        private BigDecimal savings;
    }

    /**
     * Category breakdown data
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryReportData {
        private String categoryId;
        private String name;
        private String icon;
        private String color;
        private BigDecimal amount;
        private BigDecimal percentage;
        private String trend; // up, down, stable
        private BigDecimal change;
    }

    /**
     * Top expense item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopExpenseData {
        private String id;
        private String description;
        private String category;
        private String color;
        private BigDecimal amount;
        private LocalDate date;
    }
}
