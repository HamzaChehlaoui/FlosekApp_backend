package com.flosek.flosek.controller;

import com.flosek.flosek.dto.request.ReportRequestDTO;
import com.flosek.flosek.dto.response.ReportResponseDTO;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for financial reports
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Generate a custom report based on request parameters
     *
     * @param user the authenticated user
     * @param request the report request parameters
     * @return the generated report
     */
    @PostMapping("/generate")
    public ResponseEntity<ReportResponseDTO> generateReport(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ReportRequestDTO request) {
        ReportResponseDTO report = reportService.generateReport(user.getId(), request);
        return ResponseEntity.ok(report);
    }

    /**
     * Get monthly report for current month
     *
     * @param user the authenticated user
     * @return the monthly report
     */
    @GetMapping("/monthly")
    public ResponseEntity<ReportResponseDTO> getMonthlyReport(@AuthenticationPrincipal User user) {
        ReportResponseDTO report = reportService.getMonthlyReport(user.getId());
        return ResponseEntity.ok(report);
    }

    /**
     * Get yearly report
     *
     * @param user the authenticated user
     * @param year the year (optional, defaults to current year)
     * @return the yearly report
     */
    @GetMapping("/yearly")
    public ResponseEntity<ReportResponseDTO> getYearlyReport(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer year) {
        ReportResponseDTO report = reportService.getYearlyReport(user.getId(), year);
        return ResponseEntity.ok(report);
    }

    /**
     * Get comparison report showing trends over multiple months
     *
     * @param user the authenticated user
     * @param months number of months to compare (optional, defaults to 6)
     * @return the comparison report
     */
    @GetMapping("/comparison")
    public ResponseEntity<ReportResponseDTO> getComparisonReport(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false, defaultValue = "6") Integer months) {
        ReportResponseDTO report = reportService.getComparisonReport(user.getId(), months);
        return ResponseEntity.ok(report);
    }
}
