package com.flosek.flosek.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Google Sign-In request containing the Google ID token
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginRequestDTO {

    @NotBlank(message = "Google ID token is required")
    private String idToken;
}
