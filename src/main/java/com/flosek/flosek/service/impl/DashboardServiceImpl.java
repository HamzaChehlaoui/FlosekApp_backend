package com.flosek.flosek.service.impl;

import com.flosek.flosek.dto.response.DashboardResponseDTO;
import com.flosek.flosek.dto.response.SavingsGoalResponseDTO;
import com.flosek.flosek.dto.response.SpendingCategoryDTO;
import com.flosek.flosek.dto.response.TransactionDTO;
import com.flosek.flosek.entity.Expense;
import com.flosek.flosek.entity.Salary;
import com.flosek.flosek.entity.SavingsGoal;
import com.flosek.flosek.mapper.SavingsGoalMapper;
import com.flosek.flosek.repository.BudgetRepository;
import com.flosek.flosek.repository.ExpenseRepository;
import com.flosek.flosek.repository.SalaryRepository;
import com.flosek.flosek.repository.SavingsGoalRepository;
import com.flosek.flosek.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of DashboardService
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final ExpenseRepository expenseRepository;
    private final SalaryRepository salaryRepository;
    private final BudgetRepository budgetRepository;
    private final SavingsGoalRepository savingsGoalRepository;
    private final SavingsGoalMapper savingsGoalMapper;

    private static final int RECENT_TRANSACTIONS_LIMIT = 5;
    private static final int SAVINGS_GOALS_LIMIT = 3;

    @Override
    public DashboardResponseDTO getDashboardSummary(UUID userId) {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.withDayOfMonth(today.lengthOfMonth());

        LocalDate startOfLastMonth = startOfMonth.minusMonths(1);
        LocalDate endOfLastMonth = startOfMonth.minusDays(1);

        BigDecimal monthlyIncome = salaryRepository.sumByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
        if (monthlyIncome == null) monthlyIncome = BigDecimal.ZERO;

        BigDecimal monthlyExpenses = expenseRepository.sumByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
        if (monthlyExpenses == null) monthlyExpenses = BigDecimal.ZERO;

        BigDecimal remaining = monthlyIncome.subtract(monthlyExpenses);

        BigDecimal totalIncome = salaryRepository.sumByUserIdAndDateRange(userId, LocalDate.of(2000, 1, 1), today);
        BigDecimal totalExpenses = expenseRepository.sumByUserIdAndDateRange(userId, LocalDate.of(2000, 1, 1), today);
        if (totalIncome == null) totalIncome = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;
        BigDecimal totalBalance = totalIncome.subtract(totalExpenses);

        BigDecimal lastMonthIncome = salaryRepository.sumByUserIdAndDateRange(userId, startOfLastMonth, endOfLastMonth);
        BigDecimal lastMonthExpenses = expenseRepository.sumByUserIdAndDateRange(userId, startOfLastMonth, endOfLastMonth);
        if (lastMonthIncome == null) lastMonthIncome = BigDecimal.ZERO;
        if (lastMonthExpenses == null) lastMonthExpenses = BigDecimal.ZERO;
        BigDecimal lastMonthBalance = lastMonthIncome.subtract(lastMonthExpenses);

        BigDecimal balanceChange = BigDecimal.ZERO;
        if (lastMonthBalance.compareTo(BigDecimal.ZERO) != 0) {
            balanceChange = remaining.subtract(lastMonthBalance)
                    .divide(lastMonthBalance.abs(), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        Integer expenseCategories = expenseRepository.countDistinctCategoriesByUserIdAndDateRange(userId, startOfMonth, endOfMonth);
        if (expenseCategories == null) expenseCategories = 0;

        BigDecimal budgetTotal = budgetRepository.sumActiveBudgetAmount(userId, today);
        BigDecimal budgetSpent = budgetRepository.sumActiveSpentAmount(userId, today);
        if (budgetTotal == null) budgetTotal = BigDecimal.ZERO;
        if (budgetSpent == null) budgetSpent = BigDecimal.ZERO;

        Double budgetPercentage = 0.0;
        if (budgetTotal.compareTo(BigDecimal.ZERO) > 0) {
            budgetPercentage = budgetSpent.divide(budgetTotal, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100)).doubleValue();
        }

        List<SavingsGoal> savingsGoals = savingsGoalRepository.findTopByUserId(userId, PageRequest.of(0, SAVINGS_GOALS_LIMIT));
        List<SavingsGoalResponseDTO> savingsGoalDTOs = savingsGoalMapper.toResponseDTOList(savingsGoals);

        List<TransactionDTO> recentTransactions = getRecentTransactions(userId, startOfMonth, endOfMonth);

        List<SpendingCategoryDTO> spendingCategories = getSpendingCategories(userId, startOfMonth, endOfMonth, monthlyExpenses);

        return DashboardResponseDTO.builder()
                .totalBalance(totalBalance)
                .balanceChange(balanceChange)
                .monthlyIncome(monthlyIncome)
                .monthlyExpenses(monthlyExpenses)
                .remaining(remaining)
                .expenseCategories(expenseCategories)
                .budgetTotal(budgetTotal)
                .budgetSpent(budgetSpent)
                .budgetPercentage(budgetPercentage)
                .savingsGoals(savingsGoalDTOs)
                .recentTransactions(recentTransactions)
                .spendingCategories(spendingCategories)
                .build();
    }

    /**
     * Get recent transactions (expenses + income) sorted by date
     */
    private List<TransactionDTO> getRecentTransactions(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<TransactionDTO> transactions = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // Get recent expenses
        List<Expense> expenses = expenseRepository.findRecentByUserId(userId, PageRequest.of(0, RECENT_TRANSACTIONS_LIMIT));
        for (Expense expense : expenses) {
            transactions.add(TransactionDTO.builder()
                    .id(expense.getId().toString())
                    .description(expense.getDescription() != null ? expense.getDescription() : expense.getCategory().getName())
                    .category(expense.getCategory().getName())
                    .emoji(expense.getCategory().getIcon() != null ? expense.getCategory().getIcon() : "💸")
                    .amount(expense.getAmount())
                    .type("expense")
                    .date(expense.getExpenseDate().format(formatter))
                    .build());
        }

        // Get recent salaries/income
        List<Salary> salaries = salaryRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        for (Salary salary : salaries) {
            transactions.add(TransactionDTO.builder()
                    .id(salary.getId().toString())
                    .description(salary.getDescription() != null ? salary.getDescription() : "Salary")
                    .category("Income")
                    .emoji("💰")
                    .amount(salary.getAmount())
                    .type("income")
                    .date(salary.getEffectiveDate().format(formatter))
                    .build());
        }

        // Sort by date descending and limit
        return transactions.stream()
                .sorted(Comparator.comparing(TransactionDTO::getDate).reversed())
                .limit(RECENT_TRANSACTIONS_LIMIT)
                .collect(Collectors.toList());
    }

    /**
     * Get spending by category
     */
    private List<SpendingCategoryDTO> getSpendingCategories(UUID userId, LocalDate startDate, LocalDate endDate, BigDecimal totalExpenses) {
        List<Object[]> categoryData = expenseRepository.sumByUserIdAndCategoryInDateRange(userId, startDate, endDate);
        List<SpendingCategoryDTO> categories = new ArrayList<>();

        for (Object[] row : categoryData) {
            String categoryName = (String) row[1];
            String icon = row[2] != null ? (String) row[2] : "📦";
            String color = row[3] != null ? (String) row[3] : "#6b7280";
            BigDecimal amount = (BigDecimal) row[4];

            Double percentage = 0.0;
            if (totalExpenses.compareTo(BigDecimal.ZERO) > 0) {
                percentage = amount.divide(totalExpenses, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100)).doubleValue();
            }

            categories.add(SpendingCategoryDTO.builder()
                    .name(categoryName)
                    .emoji(icon)
                    .color(color)
                    .amount(amount)
                    .percentage(percentage)
                    .build());
        }

        return categories;
    }
}
