package com.flosek.flosek.service;

import com.flosek.flosek.dto.request.ContributionRequestDTO;
import com.flosek.flosek.dto.request.SavingsGoalRequestDTO;
import com.flosek.flosek.dto.response.SavingsGoalResponseDTO;

import java.util.List;
import java.util.UUID;

public interface SavingsGoalService {

    List<SavingsGoalResponseDTO> getAllSavingsGoals(UUID userId);

    List<SavingsGoalResponseDTO> getActiveSavingsGoals(UUID userId);

    SavingsGoalResponseDTO getSavingsGoalById(UUID id, UUID userId);

    SavingsGoalResponseDTO createSavingsGoal(SavingsGoalRequestDTO request, UUID userId);

    SavingsGoalResponseDTO updateSavingsGoal(UUID id, SavingsGoalRequestDTO request, UUID userId);

    SavingsGoalResponseDTO addContribution(UUID id, ContributionRequestDTO request, UUID userId);

    void deleteSavingsGoal(UUID id, UUID userId);
}
