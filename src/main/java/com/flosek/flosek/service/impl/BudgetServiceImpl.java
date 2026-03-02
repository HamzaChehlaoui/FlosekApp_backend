package com.flosek.flosek.service.impl;

import com.flosek.flosek.dto.request.BudgetRequestDTO;
import com.flosek.flosek.dto.response.BudgetResponseDTO;
import com.flosek.flosek.entity.Budget;
import com.flosek.flosek.entity.Category;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.exception.ResourceNotFoundException;
import com.flosek.flosek.exception.UnauthorizedAccessException;
import com.flosek.flosek.mapper.BudgetMapper;
import com.flosek.flosek.repository.BudgetRepository;
import com.flosek.flosek.repository.CategoryRepository;
import com.flosek.flosek.repository.ExpenseRepository;
import com.flosek.flosek.repository.UserRepository;
import com.flosek.flosek.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final BudgetMapper budgetMapper;

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponseDTO> getAllBudgets(UUID userId) {
        List<Budget> budgets = budgetRepository.findByUserIdAndDeletedAtIsNullOrderByStartDateDesc(userId);
        return budgetMapper.toResponseDTOList(budgets);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BudgetResponseDTO> getActiveBudgets(UUID userId) {
        List<Budget> budgets = budgetRepository.findActiveByUserId(userId, LocalDate.now());
        return budgetMapper.toResponseDTOList(budgets);
    }

    @Override
    @Transactional(readOnly = true)
    public BudgetResponseDTO getBudgetById(UUID id, UUID userId) {
        Budget budget = findBudgetAndValidateOwner(id, userId);
        return budgetMapper.toResponseDTO(budget);
    }

    @Override
    @Transactional
    public BudgetResponseDTO createBudget(BudgetRequestDTO request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Calculate already spent amount for this category in the budget period
        BigDecimal alreadySpent = expenseRepository.sumByUserIdAndDateRange(
                userId, request.getStartDate(), request.getEndDate());

        Budget budget = Budget.builder()
                .amount(request.getAmount())
                .spentAmount(alreadySpent != null ? alreadySpent : BigDecimal.ZERO)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .category(category)
                .user(user)
                .name(request.getName())
                .build();

        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public BudgetResponseDTO updateBudget(UUID id, BudgetRequestDTO request, UUID userId) {
        Budget budget = findBudgetAndValidateOwner(id, userId);

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        budget.setAmount(request.getAmount());
        budget.setStartDate(request.getStartDate());
        budget.setEndDate(request.getEndDate());
        budget.setCategory(category);
        budget.setName(request.getName());

        Budget saved = budgetRepository.save(budget);
        return budgetMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteBudget(UUID id, UUID userId) {
        Budget budget = findBudgetAndValidateOwner(id, userId);
        budget.softDelete();
        budgetRepository.save(budget);
    }

    private Budget findBudgetAndValidateOwner(UUID id, UUID userId) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", "id", id));
        if (budget.isDeleted()) {
            throw new ResourceNotFoundException("Budget", "id", id);
        }
        if (!budget.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have access to this budget");
        }
        return budget;
    }
}
