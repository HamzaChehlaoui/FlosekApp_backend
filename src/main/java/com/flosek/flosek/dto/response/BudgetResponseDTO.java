package com.flosek.flosek.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * DTO for budget response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BudgetResponseDTO {

    private UUID id;
    private String name;
    private BigDecimal amount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private CategoryResponseDTO category;
    private Boolean isExceeded;
    private Double spentPercentage;
    private Boolean isRecurring;
}
