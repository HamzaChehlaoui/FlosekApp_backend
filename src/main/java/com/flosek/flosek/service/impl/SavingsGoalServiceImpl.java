package com.flosek.flosek.service.impl;

import com.flosek.flosek.dto.request.ContributionRequestDTO;
import com.flosek.flosek.dto.request.SavingsGoalRequestDTO;
import com.flosek.flosek.dto.response.ContributionResponseDTO;
import com.flosek.flosek.dto.response.SavingsGoalResponseDTO;
import com.flosek.flosek.entity.Contribution;
import com.flosek.flosek.entity.SavingsGoal;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.exception.ResourceNotFoundException;
import com.flosek.flosek.exception.UnauthorizedAccessException;
import com.flosek.flosek.mapper.SavingsGoalMapper;
import com.flosek.flosek.repository.ContributionRepository;
import com.flosek.flosek.repository.SavingsGoalRepository;
import com.flosek.flosek.repository.UserRepository;
import com.flosek.flosek.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;
    private final ContributionRepository contributionRepository;
    private final UserRepository userRepository;
    private final SavingsGoalMapper savingsGoalMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponseDTO> getAllSavingsGoals(UUID userId) {
        List<SavingsGoal> goals = savingsGoalRepository.findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId);
        return savingsGoalMapper.toResponseDTOList(goals);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavingsGoalResponseDTO> getActiveSavingsGoals(UUID userId) {
        List<SavingsGoal> goals = savingsGoalRepository.findActiveByUserId(userId);
        return savingsGoalMapper.toResponseDTOList(goals);
    }

    @Override
    @Transactional(readOnly = true)
    public SavingsGoalResponseDTO getSavingsGoalById(UUID id, UUID userId) {
        SavingsGoal goal = findGoalAndValidateOwner(id, userId);
        return savingsGoalMapper.toResponseDTO(goal);
    }

    @Override
    @Transactional
    public SavingsGoalResponseDTO createSavingsGoal(SavingsGoalRequestDTO request, UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        SavingsGoal goal = SavingsGoal.builder()
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .currentAmount(BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .description(request.getDescription())
                .icon(request.getIcon() != null ? request.getIcon() : "\uD83C\uDFAF")
                .color(request.getColor() != null ? request.getColor() : "#10b981")
                .user(user)
                .build();

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return savingsGoalMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public SavingsGoalResponseDTO updateSavingsGoal(UUID id, SavingsGoalRequestDTO request, UUID userId) {
        SavingsGoal goal = findGoalAndValidateOwner(id, userId);

        goal.setName(request.getName());
        goal.setTargetAmount(request.getTargetAmount());
        goal.setTargetDate(request.getTargetDate());
        goal.setDescription(request.getDescription());
        if (request.getIcon() != null) goal.setIcon(request.getIcon());
        if (request.getColor() != null) goal.setColor(request.getColor());

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return savingsGoalMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public SavingsGoalResponseDTO addContribution(UUID id, ContributionRequestDTO request, UUID userId) {
        SavingsGoal goal = findGoalAndValidateOwner(id, userId);

        if (goal.isCompleted()) {
            throw new IllegalArgumentException("This savings goal is already completed");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        BigDecimal currentAmount = goal.getCurrentAmount() != null ? goal.getCurrentAmount() : BigDecimal.ZERO;
        goal.setCurrentAmount(currentAmount.add(request.getAmount()));

        // Save contribution record
        Contribution contribution = Contribution.builder()
                .savingsGoal(goal)
                .user(user)
                .amount(request.getAmount())
                .note(request.getNote())
                .contributionDate(LocalDateTime.now())
                .build();
        contributionRepository.save(contribution);

        SavingsGoal saved = savingsGoalRepository.save(goal);
        return savingsGoalMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContributionResponseDTO> getRecentContributions(UUID userId, int limit) {
        List<Contribution> contributions = contributionRepository.findRecentByUserId(
                userId, PageRequest.of(0, limit));
        return contributions.stream()
                .map(this::mapContributionToDTO)
                .toList();
    }

    private ContributionResponseDTO mapContributionToDTO(Contribution contribution) {
        return ContributionResponseDTO.builder()
                .id(contribution.getId())
                .savingsGoalId(contribution.getSavingsGoal().getId())
                .goalName(contribution.getSavingsGoal().getName())
                .goalColor(contribution.getSavingsGoal().getColor())
                .amount(contribution.getAmount())
                .note(contribution.getNote())
                .contributionDate(contribution.getContributionDate())
                .createdAt(contribution.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteSavingsGoal(UUID id, UUID userId) {
        SavingsGoal goal = findGoalAndValidateOwner(id, userId);
        goal.softDelete();
        savingsGoalRepository.save(goal);
    }

    private SavingsGoal findGoalAndValidateOwner(UUID id, UUID userId) {
        SavingsGoal goal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavingsGoal", "id", id));
        if (goal.isDeleted()) {
            throw new ResourceNotFoundException("SavingsGoal", "id", id);
        }
        if (!goal.getUser().getId().equals(userId)) {
            throw new UnauthorizedAccessException("You don't have access to this savings goal");
        }
        return goal;
    }
}
