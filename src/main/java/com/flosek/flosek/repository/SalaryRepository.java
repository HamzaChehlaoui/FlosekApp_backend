package com.flosek.flosek.repository;

import com.flosek.flosek.entity.Salary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Salary entity
 */
@Repository
public interface SalaryRepository extends JpaRepository<Salary, UUID> {

       /**
       * Find current salary for a user
       */
       Optional<Salary> findByUserIdAndIsCurrentTrueAndDeletedAtIsNull(UUID userId);

       /**
       * Find all salaries for a user
       */
       List<Salary> findByUserIdAndDeletedAtIsNullOrderByEffectiveDateDesc(UUID userId);

       /**
       * Find salaries in a date range
       */
       @Query("SELECT s FROM Salary s WHERE s.user.id = :userId " +
              "AND s.effectiveDate BETWEEN :startDate AND :endDate " +
              "AND s.deletedAt IS NULL ORDER BY s.effectiveDate DESC")
       List<Salary> findByUserIdAndDateRange(
              @Param("userId") UUID userId,
              @Param("startDate") LocalDate startDate,
              @Param("endDate") LocalDate endDate);

       /**
     * Calculate total income for a user in a date range
     */
       @Query("SELECT COALESCE(SUM(s.amount), 0) FROM Salary s WHERE s.user.id = :userId " +
              "AND s.effectiveDate BETWEEN :startDate AND :endDate " +
              "AND s.deletedAt IS NULL")
       BigDecimal sumByUserIdAndDateRange(
              @Param("userId") UUID userId,
              @Param("startDate") LocalDate startDate,
              @Param("endDate") LocalDate endDate);
}
