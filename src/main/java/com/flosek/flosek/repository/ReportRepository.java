package com.flosek.flosek.repository;

import com.flosek.flosek.entity.Report;
import com.flosek.flosek.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Report entity
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    /**
     * Find all reports for a user
     */
    List<Report> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);

    /**
     * Find reports by type for a user
     */
    List<Report> findByUserIdAndReportTypeAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, ReportType reportType);

    /**
     * Find reports within a date range
     */
    @Query("SELECT r FROM Report r WHERE r.user.id = :userId " +
            "AND r.startDate >= :startDate AND r.endDate <= :endDate " +
            "AND r.deletedAt IS NULL ORDER BY r.createdAt DESC")
    List<Report> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Find the most recent report of a specific type
     */
    Optional<Report> findFirstByUserIdAndReportTypeAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId, ReportType reportType);
}
