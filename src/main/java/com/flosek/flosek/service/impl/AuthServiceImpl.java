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
import com.flosek.flosek.dto.request.GoogleLoginRequestDTO;
import com.flosek.flosek.enums.AuthProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;

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

    @Value("${google.client-id}")
    private String googleClientId;

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

    @Override
    @Transactional
    public AuthResponseDTO googleLogin(GoogleLoginRequestDTO request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(request.getIdToken());
            if (idToken == null) {
                throw new IllegalArgumentException("Invalid Google ID token.");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.getEmail();
            String firstName = (String) payload.get("given_name");
            String lastName = (String) payload.get("family_name");

            if (firstName == null) firstName = "User";
            if (lastName == null) lastName = "";

            Optional<User> userOptional = userRepository.findByEmailAndDeletedAtIsNull(email);
            User user;

            if (userOptional.isPresent()) {
                user = userOptional.get();
                // Ensure users registered locally can still login via Google
                if (user.getAuthProvider() == AuthProvider.LOCAL) {
                    user.setAuthProvider(AuthProvider.GOOGLE);
                    userRepository.save(user);
                }
            } else {
                // Register new user via Google
                user = new User();
                user.setEmail(email);
                user.setFirstName(firstName);
                user.setLastName(lastName);
                user.setRole(Role.USER);
                user.setAuthProvider(AuthProvider.GOOGLE);
                // No password needed for OAuth
                user = userRepository.save(user);
            }

            String token = jwtProvider.generateToken(user);
            return buildAuthResponse(user, token);

        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to verify Google token", e);
        }
    }

    private AuthResponseDTO buildAuthResponse(User user, String token) {
        AuthResponseDTO response = userMapper.toAuthResponseDTO(user);
        response.setAccessToken(token);
        response.setExpiresIn(jwtProvider.getExpirationTime());
        return response;
    }
}
