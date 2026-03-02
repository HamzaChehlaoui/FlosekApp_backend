package com.flosek.flosek.controller;

import com.flosek.flosek.dto.request.BudgetRequestDTO;
import com.flosek.flosek.dto.response.BudgetResponseDTO;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetResponseDTO>> getAllBudgets(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(budgetService.getAllBudgets(user.getId()));
    }

    @GetMapping("/active")
    public ResponseEntity<List<BudgetResponseDTO>> getActiveBudgets(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(budgetService.getActiveBudgets(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> getBudgetById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(budgetService.getBudgetById(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<BudgetResponseDTO> createBudget(
            @Valid @RequestBody BudgetRequestDTO request,
            @AuthenticationPrincipal User user) {
        BudgetResponseDTO response = budgetService.createBudget(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponseDTO> updateBudget(
            @PathVariable UUID id,
            @Valid @RequestBody BudgetRequestDTO request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(budgetService.updateBudget(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        budgetService.deleteBudget(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
