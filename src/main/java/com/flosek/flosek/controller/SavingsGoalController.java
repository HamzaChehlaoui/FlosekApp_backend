package com.flosek.flosek.controller;

import com.flosek.flosek.dto.request.ContributionRequestDTO;
import com.flosek.flosek.dto.request.SavingsGoalRequestDTO;
import com.flosek.flosek.dto.response.SavingsGoalResponseDTO;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/savings-goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @GetMapping
    public ResponseEntity<List<SavingsGoalResponseDTO>> getAllSavingsGoals(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(savingsGoalService.getAllSavingsGoals(user.getId()));
    }

    @GetMapping("/active")
    public ResponseEntity<List<SavingsGoalResponseDTO>> getActiveSavingsGoals(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(savingsGoalService.getActiveSavingsGoals(user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SavingsGoalResponseDTO> getSavingsGoalById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(savingsGoalService.getSavingsGoalById(id, user.getId()));
    }

    @PostMapping
    public ResponseEntity<SavingsGoalResponseDTO> createSavingsGoal(
            @Valid @RequestBody SavingsGoalRequestDTO request,
            @AuthenticationPrincipal User user) {
        SavingsGoalResponseDTO response = savingsGoalService.createSavingsGoal(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoalResponseDTO> updateSavingsGoal(
            @PathVariable UUID id,
            @Valid @RequestBody SavingsGoalRequestDTO request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(savingsGoalService.updateSavingsGoal(id, request, user.getId()));
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<SavingsGoalResponseDTO> addContribution(
            @PathVariable UUID id,
            @Valid @RequestBody ContributionRequestDTO request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(savingsGoalService.addContribution(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavingsGoal(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        savingsGoalService.deleteSavingsGoal(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
