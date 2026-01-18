package com.flosek.flosek.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for category response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {

    private UUID id;
    private String name;
    private String description;
    private String color;
    private String icon;
    private Boolean isDefault;
}
