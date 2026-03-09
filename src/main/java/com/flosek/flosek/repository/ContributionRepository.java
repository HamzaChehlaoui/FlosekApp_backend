package com.flosek.flosek.repository;

import com.flosek.flosek.entity.Contribution;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for Contribution entity
 */
@Repository
public interface ContributionRepository extends JpaRepository<Contribution, UUID> {

    /**
     * Find all contributions for a user that are not soft deleted, ordered by date
     */
    List<Contribution> findByUserIdAndDeletedAtIsNullOrderByContributionDateDesc(UUID userId);

    /**
     * Find recent contributions for a user (limited)
     */
    @Query("SELECT c FROM Contribution c WHERE c.user.id = :userId AND c.deletedAt IS NULL ORDER BY c.contributionDate DESC")
    List<Contribution> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Find contributions for a specific savings goal
     */
    List<Contribution> findBySavingsGoalIdAndDeletedAtIsNullOrderByContributionDateDesc(UUID savingsGoalId);
}
