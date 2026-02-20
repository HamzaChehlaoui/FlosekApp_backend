package com.flosek.flosek.service;

import com.flosek.flosek.dto.response.DashboardResponseDTO;

import java.util.UUID;

/**
 * Service interface for dashboard operations
 */
public interface DashboardService {

    /**
     * Get dashboard summary for a user
     *
     * @param userId the user ID
     * @return dashboard data including balance, expenses, income, savings goals, etc.
     */
    DashboardResponseDTO getDashboardSummary(UUID userId);
}
