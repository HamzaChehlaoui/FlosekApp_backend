package com.flosek.flosek.repository;

import com.flosek.flosek.entity.Expense;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Expense entity
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    /**
     * Find all expenses for a user that are not soft deleted
     */
    List<Expense> findByUserIdAndDeletedAtIsNullOrderByExpenseDateDesc(UUID userId);

    /**
     * Find expenses by user within a date range
     */
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.deletedAt IS NULL ORDER BY e.expenseDate DESC")
    List<Expense> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find recent expenses for a user with limit
     */
    @Query("SELECT e FROM Expense e WHERE e.user.id = :userId " +
           "AND e.deletedAt IS NULL ORDER BY e.expenseDate DESC, e.createdAt DESC")
    List<Expense> findRecentByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Calculate total expenses for a user in a date range
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.deletedAt IS NULL")
    BigDecimal sumByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Calculate total expenses by category for a user in a date range
     */
    @Query("SELECT e.category.id, e.category.name, e.category.icon, e.category.color, " +
           "COALESCE(SUM(e.amount), 0) " +
           "FROM Expense e WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.deletedAt IS NULL " +
           "GROUP BY e.category.id, e.category.name, e.category.icon, e.category.color " +
           "ORDER BY SUM(e.amount) DESC")
    List<Object[]> sumByUserIdAndCategoryInDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Count distinct categories used by user in a date range
     */
    @Query("SELECT COUNT(DISTINCT e.category.id) FROM Expense e WHERE e.user.id = :userId " +
           "AND e.expenseDate BETWEEN :startDate AND :endDate " +
           "AND e.deletedAt IS NULL")
    Integer countDistinctCategoriesByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
