package com.flosek.flosek.service.impl;

import com.flosek.flosek.dto.request.ExpenseRequestDTO;
import com.flosek.flosek.dto.response.ExpenseResponseDTO;
import com.flosek.flosek.entity.Budget;
import com.flosek.flosek.entity.Category;
import com.flosek.flosek.entity.Expense;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.exception.ResourceNotFoundException;
import com.flosek.flosek.exception.UnauthorizedAccessException;
import com.flosek.flosek.mapper.ExpenseMapper;
import com.flosek.flosek.repository.BudgetRepository;
import com.flosek.flosek.repository.CategoryRepository;
import com.flosek.flosek.repository.ExpenseRepository;
import com.flosek.flosek.repository.UserRepository;
import com.flosek.flosek.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final BudgetRepository budgetRepository;
    private final ExpenseMapper expenseMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getAllExpenses(UUID userId) {
        List<Expense> expenses = expenseRepository.findByUserIdAndDeletedAtIsNullOrderByExpenseDateDesc(userId);
        return expenseMapper.toResponseDTOList(expenses);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExpenseResponseDTO> getExpensesByDateRange(UUID userId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseRepository.findByUserIdAndDateRange(userId, startDate, endDate);
        return expenseMapper.toResponseDTOList(expenses);
    }

    @Override
    @Transactional(readOnly = true)
    public ExpenseResponseDTO getExpenseById(UUID id, UUID userId) {
        Expense expense = findExpenseAndValidateOwner(id, userId);
        return expenseMapper.toResponseDTO(expense);
    }

    @Override
    @Transactional
    public ExpenseResponseDTO createExpense(ExpenseRequestDTO request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Expense expense = Expense.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .expenseDate(request.getExpenseDate())
                .category(category)
                .user(user)
                .notes(request.getNotes())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .build();

        Expense saved = expenseRepository.save(expense);

        // Update related budget spent amount
        updateBudgetSpentAmount(userId, category.getId(), request.getAmount(), request.getExpenseDate());

        return expenseMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ExpenseResponseDTO updateExpense(UUID id, ExpenseRequestDTO request, UUID userId) {
        Expense expense = findExpenseAndValidateOwner(id, userId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        updateBudgetSpentAmount(userId, expense.getCategory().getId(),
                expense.getAmount().negate(), expense.getExpenseDate());

        expense.setAmount(request.getAmount());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setCategory(category);
        expense.setNotes(request.getNotes());
        expense.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);

        Expense saved = expenseRepository.save(expense);

        updateBudgetSpentAmount(userId, category.getId(), request.getAmount(), request.getExpenseDate());

        return expenseMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteExpense(UUID id, UUID userId) {
        Expense expense = findExpenseAndValidateOwner(id, userId);

        updateBudgetSpentAmount(userId, expense.getCategory().getId(),
                expense.getAmount().negate(), expense.getExpenseDate());

        expense.softDelete();
        expenseRepository.save(expense);
    }

    private Expense findExpenseAndValidateOwner(UUID id, UUID userId) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", "id", id));
        if (expense.isDeleted()) {
            throw new ResourceNotFoundException("Expense", "id", id);
        }
        if (!expense.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have access to this expense");
        }
        return expense;
    }

    private void updateBudgetSpentAmount(UUID userId, UUID categoryId, BigDecimal amount, LocalDate date) {
        List<Budget> activeBudgets = budgetRepository.findActiveByUserId(userId, date);
        activeBudgets.stream()
                .filter(b -> b.getCategory().getId().equals(categoryId))
                .forEach(budget -> {
                    BigDecimal currentSpent = budget.getSpentAmount() != null ? budget.getSpentAmount() : BigDecimal.ZERO;
                    budget.setSpentAmount(currentSpent.add(amount));
                    budgetRepository.save(budget);
                });
    }
}
