package com.flosek.flosek.service;

import com.flosek.flosek.dto.request.BudgetRequestDTO;
import com.flosek.flosek.dto.response.BudgetResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BudgetService {

    List<BudgetResponseDTO> getAllBudgets(UUID userId);

    List<BudgetResponseDTO> getActiveBudgets(UUID userId);

    BudgetResponseDTO getBudgetById(UUID id, UUID userId);

    BudgetResponseDTO createBudget(BudgetRequestDTO request, UUID userId);

    BudgetResponseDTO updateBudget(UUID id, BudgetRequestDTO request, UUID userId);

    void deleteBudget(UUID id, UUID userId);
}
