package com.flosek.flosek.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for contribution response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContributionResponseDTO {

    private UUID id;
    private UUID savingsGoalId;
    private String goalName;
    private String goalColor;
    private BigDecimal amount;
    private String note;
    private LocalDateTime contributionDate;
    private LocalDateTime createdAt;
}
