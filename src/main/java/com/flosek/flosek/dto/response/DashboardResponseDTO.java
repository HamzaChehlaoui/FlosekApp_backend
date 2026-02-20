package com.flosek.flosek.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for dashboard summary response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {

    // Financial Summary
    private BigDecimal totalBalance;
    private BigDecimal balanceChange; // Percentage change from last month
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyExpenses;
    private BigDecimal remaining;
    private Integer expenseCategories;

    // Budget Summary
    private BigDecimal budgetTotal;
    private BigDecimal budgetSpent;
    private Double budgetPercentage;

    // Savings Goals
    private List<SavingsGoalResponseDTO> savingsGoals;

    // Recent Transactions (Expenses + Income)
    private List<TransactionDTO> recentTransactions;

    // Spending by Category
    private List<SpendingCategoryDTO> spendingCategories;
}
