package com.flosek.flosek.repository;

import com.flosek.flosek.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Budget entity
 */
@Repository
public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    /**
     * Find all budgets for a user that are not soft deleted
     */
    List<Budget> findByUserIdAndDeletedAtIsNullOrderByStartDateDesc(UUID userId);

    /**
     * Find active budgets for a user (current date within budget period)
     */
    @Query("SELECT b FROM Budget b WHERE b.user.id = :userId " +
           "AND :currentDate BETWEEN b.startDate AND b.endDate " +
           "AND b.deletedAt IS NULL")
    List<Budget> findActiveByUserId(
            @Param("userId") UUID userId,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Calculate total budget amount for active budgets
     */
    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Budget b WHERE b.user.id = :userId " +
           "AND :currentDate BETWEEN b.startDate AND b.endDate " +
           "AND b.deletedAt IS NULL")
    BigDecimal sumActiveBudgetAmount(
            @Param("userId") UUID userId,
            @Param("currentDate") LocalDate currentDate);

    /**
     * Calculate total spent amount for active budgets
     */
    @Query("SELECT COALESCE(SUM(b.spentAmount), 0) FROM Budget b WHERE b.user.id = :userId " +
           "AND :currentDate BETWEEN b.startDate AND b.endDate " +
           "AND b.deletedAt IS NULL")
    BigDecimal sumActiveSpentAmount(
            @Param("userId") UUID userId,
            @Param("currentDate") LocalDate currentDate);
}
