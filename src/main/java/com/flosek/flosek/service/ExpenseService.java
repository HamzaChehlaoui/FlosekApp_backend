package com.flosek.flosek.service;

import com.flosek.flosek.dto.request.ExpenseRequestDTO;
import com.flosek.flosek.dto.response.ExpenseResponseDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExpenseService {

    List<ExpenseResponseDTO> getAllExpenses(UUID userId);

    List<ExpenseResponseDTO> getExpensesByDateRange(UUID userId, LocalDate startDate, LocalDate endDate);

    ExpenseResponseDTO getExpenseById(UUID id, UUID userId);

    ExpenseResponseDTO createExpense(ExpenseRequestDTO request, UUID userId);

    ExpenseResponseDTO updateExpense(UUID id, ExpenseRequestDTO request, UUID userId);

    void deleteExpense(UUID id, UUID userId);
}
