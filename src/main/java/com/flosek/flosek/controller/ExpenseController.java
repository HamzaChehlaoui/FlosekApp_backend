package com.flosek.flosek.controller;

import com.flosek.flosek.dto.request.ExpenseRequestDTO;
import com.flosek.flosek.dto.response.ExpenseResponseDTO;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping
    public ResponseEntity<List<ExpenseResponseDTO>> getAllExpenses(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(expenseService.getExpensesByDateRange(user.getId(), startDate, endDate));
        }
        return ResponseEntity.ok(expenseService.getAllExpenses(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> getExpenseById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.getExpenseById(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponseDTO> createExpense(
            @Valid @RequestBody ExpenseRequestDTO request,
            @AuthenticationPrincipal User user) {
        ExpenseResponseDTO response = expenseService.createExpense(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponseDTO> updateExpense(
            @PathVariable UUID id,
            @Valid @RequestBody ExpenseRequestDTO request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(expenseService.updateExpense(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        expenseService.deleteExpense(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
