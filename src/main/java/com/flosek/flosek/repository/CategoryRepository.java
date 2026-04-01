package com.flosek.flosek.repository;

import com.flosek.flosek.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Category entity
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {

       /**
        * Find all categories for a user (including default categories)
        */
       @Query("SELECT c FROM Category c WHERE (c.user.id = :userId OR c.isDefault = true) " +
              "AND c.deletedAt IS NULL ORDER BY c.name")
       List<Category> findByUserIdOrDefault(@Param("userId") UUID userId);

       /**
        * Find default categories
        */
       List<Category> findByIsDefaultTrueAndDeletedAtIsNull();

       /**
        * Find category by name and user
        */
       Optional<Category> findByNameAndUserIdAndDeletedAtIsNull(String name, UUID userId);

       /**
        * Find category by id if it is user-owned or default and not deleted.
        */
       @Query("SELECT c FROM Category c WHERE c.id = :categoryId " +
              "AND (c.user.id = :userId OR c.isDefault = true) " +
              "AND c.deletedAt IS NULL")
       Optional<Category> findByIdAndUserIdOrDefault(@Param("categoryId") UUID categoryId,
                                                        @Param("userId") UUID userId);
}
