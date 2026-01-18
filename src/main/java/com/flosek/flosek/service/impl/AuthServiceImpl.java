package com.flosek.flosek.service.impl;

import com.flosek.flosek.dto.request.LoginRequestDTO;
import com.flosek.flosek.dto.request.RegisterRequestDTO;
import com.flosek.flosek.dto.response.AuthResponseDTO;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.enums.Role;
import com.flosek.flosek.exception.ResourceNotFoundException;
import com.flosek.flosek.repository.UserRepository;
import com.flosek.flosek.security.JwtProvider;
import com.flosek.flosek.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Authentication service implementation
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final com.flosek.flosek.mapper.UserMapper userMapper;

    @Override
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        String token = jwtProvider.generateToken(savedUser);

        return buildAuthResponse(savedUser, token);
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userRepository.findByEmailAndDeletedAtIsNull(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String token = jwtProvider.generateToken(user);

        return buildAuthResponse(user, token);
    }

    private AuthResponseDTO buildAuthResponse(User user, String token) {
        AuthResponseDTO response = userMapper.toAuthResponseDTO(user);
        response.setAccessToken(token);
        response.setExpiresIn(jwtProvider.getExpirationTime());
        return response;
    }
}
