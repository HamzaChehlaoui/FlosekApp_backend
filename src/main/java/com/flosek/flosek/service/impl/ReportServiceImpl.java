package com.flosek.flosek.service.impl;

import com.flosek.flosek.dto.request.ReportRequestDTO;
import com.flosek.flosek.dto.response.ReportResponseDTO;
import com.flosek.flosek.dto.response.ReportResponseDTO.CategoryReportData;
import com.flosek.flosek.dto.response.ReportResponseDTO.MonthlyReportData;
import com.flosek.flosek.dto.response.ReportResponseDTO.TopExpenseData;
import com.flosek.flosek.entity.Expense;
import com.flosek.flosek.enums.ReportType;
import com.flosek.flosek.repository.ExpenseRepository;
import com.flosek.flosek.repository.SalaryRepository;
import com.flosek.flosek.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Implementation of ReportService for generating financial reports
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private final ExpenseRepository expenseRepository;
    private final SalaryRepository salaryRepository;

    private static final int TOP_EXPENSES_LIMIT = 10;
    private static final int DEFAULT_MONTHS_COMPARISON = 6;

    @Override
    public ReportResponseDTO generateReport(UUID userId, ReportRequestDTO request) {
        LocalDate startDate;
        LocalDate endDate;

        switch (request.getReportType()) {
            case MONTHLY:
                int month = request.getMonth() != null ? request.getMonth() : LocalDate.now().getMonthValue();
                int year = request.getYear() != null ? request.getYear() : LocalDate.now().getYear();
                startDate = LocalDate.of(year, month, 1);
                endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                break;

            case YEARLY:
                int reportYear = request.getYear() != null ? request.getYear() : LocalDate.now().getYear();
                startDate = LocalDate.of(reportYear, 1, 1);
                endDate = LocalDate.of(reportYear, 12, 31);
                break;

            case QUARTERLY:
                int qYear = request.getYear() != null ? request.getYear() : LocalDate.now().getYear();
                int quarter = request.getQuarter() != null ? request.getQuarter() : ((LocalDate.now().getMonthValue() - 1) / 3) + 1;
                int startMonth = (quarter - 1) * 3 + 1;
                startDate = LocalDate.of(qYear, startMonth, 1);
                endDate = startDate.plusMonths(2).withDayOfMonth(startDate.plusMonths(2).lengthOfMonth());
                break;

            case CUSTOM:
                startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now().minusMonths(1);
                endDate = request.getEndDate() != null ? request.getEndDate() : LocalDate.now();
                break;

            default:
                startDate = LocalDate.now().withDayOfMonth(1);
                endDate = LocalDate.now();
        }

        return buildReport(userId, request.getReportType(), startDate, endDate);
    }

    @Override
    public ReportResponseDTO getMonthlyReport(UUID userId) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

        return buildReport(userId, ReportType.MONTHLY, startDate, endDate);
    }

    @Override
    public ReportResponseDTO getYearlyReport(UUID userId, Integer year) {
        int reportYear = year != null ? year : LocalDate.now().getYear();
        LocalDate startDate = LocalDate.of(reportYear, 1, 1);
        LocalDate endDate = LocalDate.of(reportYear, 12, 31);

        return buildReport(userId, ReportType.YEARLY, startDate, endDate);
    }

    @Override
    public ReportResponseDTO getComparisonReport(UUID userId, Integer months) {
        int numMonths = months != null ? months : DEFAULT_MONTHS_COMPARISON;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusMonths(numMonths - 1).withDayOfMonth(1);
        LocalDate endDate = today.withDayOfMonth(today.lengthOfMonth());

        return buildReport(userId, ReportType.COMPARISON, startDate, endDate);
    }

    /**
     * Build the report with all data
     */
    private ReportResponseDTO buildReport(UUID userId, ReportType reportType, LocalDate startDate, LocalDate endDate) {
        // Calculate totals for the period
        BigDecimal totalIncome = salaryRepository.sumByUserIdAndDateRange(userId, startDate, endDate);
        BigDecimal totalExpenses = expenseRepository.sumByUserIdAndDateRange(userId, startDate, endDate);

        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        BigDecimal netSavings = totalIncome.subtract(totalExpenses);
        BigDecimal savingsRate = BigDecimal.ZERO;
        if (totalIncome.compareTo(BigDecimal.ZERO) > 0) {
            savingsRate = netSavings.divide(totalIncome, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // Calculate changes from previous period
        long periodDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        LocalDate prevStartDate = startDate.minusDays(periodDays);
        LocalDate prevEndDate = startDate.minusDays(1);

        BigDecimal prevIncome = salaryRepository.sumByUserIdAndDateRange(userId, prevStartDate, prevEndDate);
        BigDecimal prevExpenses = expenseRepository.sumByUserIdAndDateRange(userId, prevStartDate, prevEndDate);

        if (prevIncome == null) prevIncome = BigDecimal.ZERO;
        if (prevExpenses == null) prevExpenses = BigDecimal.ZERO;

        BigDecimal incomeChange = calculatePercentageChange(prevIncome, totalIncome);
        BigDecimal expenseChange = calculatePercentageChange(prevExpenses, totalExpenses);
        BigDecimal prevSavings = prevIncome.subtract(prevExpenses);
        BigDecimal savingsChange = calculatePercentageChange(prevSavings, netSavings);

        // Get monthly data
        List<MonthlyReportData> monthlyData = getMonthlyData(userId, startDate, endDate);

        // Get category breakdown
        List<CategoryReportData> categoryBreakdown = getCategoryBreakdown(userId, startDate, endDate, totalExpenses, prevStartDate, prevEndDate);

        // Get top expenses
        List<TopExpenseData> topExpenses = getTopExpenses(userId, startDate, endDate);

        // Build period label
        String periodLabel = buildPeriodLabel(reportType, startDate, endDate);

        return ReportResponseDTO.builder()
                .reportType(reportType)
                .startDate(startDate)
                .endDate(endDate)
                .periodLabel(periodLabel)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netSavings(netSavings)
                .savingsRate(savingsRate)
                .incomeChange(incomeChange)
                .expenseChange(expenseChange)
                .savingsChange(savingsChange)
                .monthlyData(monthlyData)
                .categoryBreakdown(categoryBreakdown)
                .topExpenses(topExpenses)
                .build();
    }

    /**
     * Calculate percentage change between two values
     */
    private BigDecimal calculatePercentageChange(BigDecimal previous, BigDecimal current) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current != null && current.compareTo(BigDecimal.ZERO) != 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous.abs(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Get monthly breakdown data
     */
    private List<MonthlyReportData> getMonthlyData(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<MonthlyReportData> monthlyData = new ArrayList<>();

        LocalDate currentDate = startDate.withDayOfMonth(1);
        while (!currentDate.isAfter(endDate)) {
            LocalDate monthStart = currentDate.withDayOfMonth(1);
            LocalDate monthEnd = currentDate.withDayOfMonth(currentDate.lengthOfMonth());

            // Cap the end date if it exceeds report period
            if (monthEnd.isAfter(endDate)) {
                monthEnd = endDate;
            }

            BigDecimal income = salaryRepository.sumByUserIdAndDateRange(userId, monthStart, monthEnd);
            BigDecimal expenses = expenseRepository.sumByUserIdAndDateRange(userId, monthStart, monthEnd);

            if (income == null) income = BigDecimal.ZERO;
            if (expenses == null) expenses = BigDecimal.ZERO;

            monthlyData.add(MonthlyReportData.builder()
                    .month(currentDate.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .year(currentDate.getYear())
                    .income(income)
                    .expenses(expenses)
                    .savings(income.subtract(expenses))
                    .build());

            currentDate = currentDate.plusMonths(1);
        }

        return monthlyData;
    }

    /**
     * Get category breakdown with trends
     */
    private List<CategoryReportData> getCategoryBreakdown(UUID userId, LocalDate startDate, LocalDate endDate,
                                                           BigDecimal totalExpenses, LocalDate prevStartDate, LocalDate prevEndDate) {
        List<Object[]> categoryData = expenseRepository.sumByUserIdAndCategoryInDateRange(userId, startDate, endDate);
        List<Object[]> prevCategoryData = expenseRepository.sumByUserIdAndCategoryInDateRange(userId, prevStartDate, prevEndDate);

        List<CategoryReportData> breakdown = new ArrayList<>();

        for (Object[] row : categoryData) {
            UUID categoryId = (UUID) row[0];
            String categoryName = (String) row[1];
            String icon = row[2] != null ? (String) row[2] : "📦";
            String color = row[3] != null ? (String) row[3] : "#6b7280";
            BigDecimal amount = (BigDecimal) row[4];

            BigDecimal percentage = BigDecimal.ZERO;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }

            // Find previous period amount for this category
            BigDecimal prevAmount = BigDecimal.ZERO;
            for (Object[] prevRow : prevCategoryData) {
                if (categoryId.equals(prevRow[0])) {
                    prevAmount = (BigDecimal) prevRow[4];
                    break;
                }
            }

            BigDecimal change = calculatePercentageChange(prevAmount, amount);
            String trend = change.compareTo(BigDecimal.ZERO) > 0 ? "up" :
                          change.compareTo(BigDecimal.ZERO) < 0 ? "down" : "stable";

            breakdown.add(CategoryReportData.builder()
                    .categoryId(categoryId.toString())
                    .name(categoryName)
                    .icon(icon)
                    .color(color)
                    .amount(amount)
                    .percentage(percentage)
                    .trend(trend)
                    .change(change.abs())
                    .build());
        }

        return breakdown;
    }

    /**
     * Get top expenses in the period
     */
    private List<TopExpenseData> getTopExpenses(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByUserIdAndDateRange(userId, startDate, endDate);

        return expenses.stream()
                .sorted((e1, e2) -> e2.getAmount().compareTo(e1.getAmount()))
                .limit(TOP_EXPENSES_LIMIT)
                .map(expense -> TopExpenseData.builder()
                        .id(expense.getId().toString())
                        .description(expense.getDescription() != null ? expense.getDescription() : expense.getCategory().getName())
                        .category(expense.getCategory().getName())
                        .color(expense.getCategory().getColor() != null ? expense.getCategory().getColor() : "#6b7280")
                        .amount(expense.getAmount())
                        .date(expense.getExpenseDate())
                        .build())
                .toList();
    }

    /**
     * Build a human-readable period label
     */
    private String buildPeriodLabel(ReportType reportType, LocalDate startDate, LocalDate endDate) {
        return switch (reportType) {
            case MONTHLY -> startDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + startDate.getYear();
            case YEARLY -> "Year " + startDate.getYear();
            case QUARTERLY -> {
                int quarter = ((startDate.getMonthValue() - 1) / 3) + 1;
                yield "Q" + quarter + " " + startDate.getYear();
            }
            default -> startDate + " - " + endDate;
        };
    }
}
