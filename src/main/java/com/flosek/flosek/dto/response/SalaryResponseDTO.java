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
 * DTO for salary response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalaryResponseDTO {

    private UUID id;
    private BigDecimal amount;
    private String currency;
    private LocalDate effectiveDate;
    private String description;
    private Boolean isCurrent;
    private LocalDateTime createdAt;
}
