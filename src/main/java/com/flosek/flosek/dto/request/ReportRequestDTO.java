package com.flosek.flosek.dto.request;

import com.flosek.flosek.enums.ReportType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for generating reports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDTO {

    @NotNull(message = "Report type is required")
    private ReportType reportType;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer year;

    private Integer month;

    private Integer quarter;
}
