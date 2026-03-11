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

    /**
     * Find recurring budgets that have expired (endDate before given date)
     */
    @Query("SELECT b FROM Budget b WHERE b.isRecurring = true " +
           "AND b.endDate < :currentDate " +
           "AND b.deletedAt IS NULL")
    List<Budget> findExpiredRecurringBudgets(@Param("currentDate") LocalDate currentDate);

    /**
     * Check if an active budget already exists for a category and user in an overlapping period
     */
    @Query("SELECT COUNT(b) > 0 FROM Budget b WHERE b.user.id = :userId " +
           "AND b.category.id = :categoryId " +
           "AND b.startDate <= :endDate AND b.endDate >= :startDate " +
           "AND b.deletedAt IS NULL")
    boolean existsOverlappingBudget(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Check overlapping budget excluding a specific budget (for updates)
     */
    @Query("SELECT COUNT(b) > 0 FROM Budget b WHERE b.user.id = :userId " +
           "AND b.category.id = :categoryId " +
           "AND b.id <> :excludeId " +
           "AND b.startDate <= :endDate AND b.endDate >= :startDate " +
           "AND b.deletedAt IS NULL")
    boolean existsOverlappingBudgetExcluding(
            @Param("userId") UUID userId,
            @Param("categoryId") UUID categoryId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludeId") UUID excludeId);
}
