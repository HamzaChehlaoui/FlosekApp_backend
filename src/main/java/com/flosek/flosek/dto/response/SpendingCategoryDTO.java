package com.flosek.flosek.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for spending category summary
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpendingCategoryDTO {
    private String name;
    private String emoji;
    private String color;
    private BigDecimal amount;
    private Double percentage;
}
