package com.flosek.flosek.controller;

import com.flosek.flosek.dto.response.DashboardResponseDTO;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for dashboard operations
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * Get dashboard summary for the authenticated user
     *
     * @param user the authenticated user
     * @return dashboard data
     */
    @GetMapping
    public ResponseEntity<DashboardResponseDTO> getDashboard(@AuthenticationPrincipal User user) {
        DashboardResponseDTO dashboard = dashboardService.getDashboardSummary(user.getId());
        return ResponseEntity.ok(dashboard);
    }
}
