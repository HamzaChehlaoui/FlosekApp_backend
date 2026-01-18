package com.flosek.flosek.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for savings goal response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalResponseDTO {

    private UUID id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate targetDate;
    private String description;
    private String icon;
    private String color;
    private Double progressPercentage;
    private Boolean isCompleted;
    private LocalDateTime createdAt;
}
