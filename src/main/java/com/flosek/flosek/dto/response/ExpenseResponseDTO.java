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
 * DTO for expense response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponseDTO {

    private UUID id;
    private BigDecimal amount;
    private String description;
    private LocalDate expenseDate;
    private CategoryResponseDTO category;
    private String notes;
    private Boolean isRecurring;
    private LocalDateTime createdAt;
}
