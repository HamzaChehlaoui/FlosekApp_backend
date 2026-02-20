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

@RestController
@RequestMapping("/api/salaries")
@RequiredArgsConstructor
public class SalaryController {

    private final SalaryService salaryService;

    @PostMapping
    public ResponseEntity<SalaryResponseDTO> createSalary(
            @Valid @RequestBody SalaryRequestDTO request,
            @AuthenticationPrincipal User user) {
        SalaryResponseDTO response = salaryService.createSalary(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
