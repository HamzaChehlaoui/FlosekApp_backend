package com.flosek.flosek.repository;

import com.flosek.flosek.entity.SavingsGoal;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for SavingsGoal entity
 */
@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, UUID> {

       /**
        * Find all savings goals for a user that are not soft deleted
        */
       List<SavingsGoal> findByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID userId);

       /**
        * Find active (not completed) savings goals for a user
        */
       @Query("SELECT sg FROM SavingsGoal sg WHERE sg.user.id = :userId " +
              "AND sg.currentAmount < sg.targetAmount " +
              "AND sg.deletedAt IS NULL ORDER BY sg.createdAt DESC")
       List<SavingsGoal> findActiveByUserId(@Param("userId") UUID userId);

       /**
        * Find top savings goals with limit
        */
       @Query("SELECT sg FROM SavingsGoal sg WHERE sg.user.id = :userId " +
              "AND sg.deletedAt IS NULL ORDER BY sg.createdAt DESC")
       List<SavingsGoal> findTopByUserId(@Param("userId") UUID userId, Pageable pageable);
}
