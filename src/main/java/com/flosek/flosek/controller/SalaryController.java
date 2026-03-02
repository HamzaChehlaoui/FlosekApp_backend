package com.flosek.flosek.controller;

import com.flosek.flosek.dto.request.SalaryRequestDTO;
import com.flosek.flosek.dto.response.SalaryResponseDTO;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.service.SalaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @GetMapping
    public ResponseEntity<List<SalaryResponseDTO>> getAllSalaries(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(salaryService.getAllSalaries(user.getId()));
    }

    @GetMapping("/current")
    public ResponseEntity<SalaryResponseDTO> getCurrentSalary(
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(salaryService.getCurrentSalary(user.getId()));
    }

    @PostMapping
    public ResponseEntity<SalaryResponseDTO> createSalary(
            @Valid @RequestBody SalaryRequestDTO request,
            @AuthenticationPrincipal User user) {
        SalaryResponseDTO response = salaryService.createSalary(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaryResponseDTO> updateSalary(
            @PathVariable UUID id,
            @Valid @RequestBody SalaryRequestDTO request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(salaryService.updateSalary(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSalary(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        salaryService.deleteSalary(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
