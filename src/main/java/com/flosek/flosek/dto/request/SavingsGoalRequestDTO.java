package com.flosek.flosek.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for savings goal creation/update request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SavingsGoalRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Target amount is required")
    @DecimalMin(value = "0.01", message = "Target amount must be greater than 0")
    private BigDecimal targetAmount;

    private LocalDate targetDate;
    private String description;
    private String icon;
    private String color;
}
