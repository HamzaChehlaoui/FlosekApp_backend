package com.flosek.flosek.service;

import com.flosek.flosek.dto.request.LoginRequestDTO;
import com.flosek.flosek.dto.request.RegisterRequestDTO;
import com.flosek.flosek.dto.response.AuthResponseDTO;

/**
 * Authentication service interface
 */
public interface AuthService {

    /**
     * Register a new user
     */
    AuthResponseDTO register(RegisterRequestDTO request);

    /**
     * Authenticate user and return JWT token
     */
    AuthResponseDTO login(LoginRequestDTO request);
}
