package com.flosek.flosek.service;

import com.flosek.flosek.dto.request.ReportRequestDTO;
import com.flosek.flosek.dto.response.ReportResponseDTO;

import java.util.UUID;

/**
 * Service interface for generating financial reports
 */
public interface ReportService {

    /**
     * Generate a financial report based on the request parameters
     *
     * @param userId the user ID
     * @param request the report request parameters
     * @return the generated report data
     */
    ReportResponseDTO generateReport(UUID userId, ReportRequestDTO request);

    /**
     * Generate a monthly report for the current month
     *
     * @param userId the user ID
     * @return the monthly report data
     */
    ReportResponseDTO getMonthlyReport(UUID userId);

    /**
     * Generate a yearly report for the current year
     *
     * @param userId the user ID
     * @param year the year to generate report for
     * @return the yearly report data
     */
    ReportResponseDTO getYearlyReport(UUID userId, Integer year);

    /**
     * Get report with comparison to previous period
     *
     * @param userId the user ID
     * @param months number of months to compare
     * @return the comparison report data
     */
    ReportResponseDTO getComparisonReport(UUID userId, Integer months);
}
