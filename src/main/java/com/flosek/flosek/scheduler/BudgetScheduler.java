package com.flosek.flosek.scheduler;

import com.flosek.flosek.entity.Budget;
import com.flosek.flosek.repository.BudgetRepository;
import com.flosek.flosek.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BudgetScheduler {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;

    /**
     * Runs at midnight on the 1st of every month.
     * Finds expired recurring budgets and creates new ones for the current month.
     */
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void renewRecurringBudgets() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        List<Budget> expiredRecurring = budgetRepository.findExpiredRecurringBudgets(today);
        log.info("Found {} expired recurring budgets to renew", expiredRecurring.size());

        for (Budget old : expiredRecurring) {
            // Skip if an overlapping budget already exists for this category
            if (budgetRepository.existsOverlappingBudget(
                    old.getUser().getId(), old.getCategory().getId(), monthStart, monthEnd)) {
                log.info("Skipping renewal for budget '{}' — overlapping budget exists", old.getName());
                continue;
            }

            BigDecimal alreadySpent = expenseRepository.sumByUserIdAndCategoryIdAndDateRange(
                    old.getUser().getId(), old.getCategory().getId(), monthStart, monthEnd);

            Budget renewed = Budget.builder()
                    .amount(old.getAmount())
                    .spentAmount(alreadySpent != null ? alreadySpent : BigDecimal.ZERO)
                    .startDate(monthStart)
                    .endDate(monthEnd)
                    .category(old.getCategory())
                    .user(old.getUser())
                    .name(old.getName())
                    .isRecurring(true)
                    .build();

            budgetRepository.save(renewed);
            log.info("Renewed recurring budget '{}' for {}-{}", old.getName(), monthStart, monthEnd);
        }
    }
}
