package com.flosek.flosek.service.impl;

import com.flosek.flosek.dto.request.GoogleLoginRequestDTO;
import com.flosek.flosek.dto.request.LoginRequestDTO;
import com.flosek.flosek.dto.request.RegisterRequestDTO;
import com.flosek.flosek.dto.response.AuthResponseDTO;
import com.flosek.flosek.entity.User;
import com.flosek.flosek.enums.AuthProvider;
import com.flosek.flosek.enums.Role;
import com.flosek.flosek.exception.ResourceNotFoundException;
import com.flosek.flosek.mapper.UserMapper;
import com.flosek.flosek.repository.UserRepository;
import com.flosek.flosek.security.JwtProvider;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.UUID;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtProvider jwtProvider;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private AuthServiceImpl authService;

    // ──────────────────────────────────────────────
    // Shared test data
    // ──────────────────────────────────────────────
    private User mockUser;
    private AuthResponseDTO mockAuthResponse;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "googleClientId", "test-google-client-id");

        mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("test@example.com");
        mockUser.setFirstName("John");
        mockUser.setLastName("Doe");
        mockUser.setRole(Role.USER);
        mockUser.setAuthProvider(AuthProvider.LOCAL);

        mockAuthResponse = new AuthResponseDTO();
        mockAuthResponse.setAccessToken("mock-jwt-token");
        mockAuthResponse.setExpiresIn(3600L);
    }

    // ══════════════════════════════════════════════
    // REGISTER
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("register()")
    class Register {

        private RegisterRequestDTO buildRequest() {
            RegisterRequestDTO req = new RegisterRequestDTO();
            req.setEmail("test@example.com");
            req.setPassword("password123");
            req.setFirstName("John");
            req.setLastName("Doe");
            return req;
        }

        @Test
        @DisplayName("✅ Success — new email, returns token")
        void register_success() {
            RegisterRequestDTO request = buildRequest();

            when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(mockUser);
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
            when(userRepository.save(mockUser)).thenReturn(mockUser);
            when(jwtProvider.generateToken(mockUser)).thenReturn("mock-jwt-token");
            when(jwtProvider.getExpirationTime()).thenReturn(3600L);
            when(userMapper.toAuthResponseDTO(mockUser)).thenReturn(mockAuthResponse);

            AuthResponseDTO result = authService.register(request);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("mock-jwt-token");
            assertThat(result.getExpiresIn()).isEqualTo(3600L);

            verify(userRepository).existsByEmail(request.getEmail());
            verify(passwordEncoder).encode(request.getPassword());
            verify(userRepository).save(mockUser);
            verify(jwtProvider).generateToken(mockUser);
        }

        @Test
        @DisplayName("❌ Duplicate email — throws IllegalArgumentException")
        void register_duplicateEmail_throwsException() {
            RegisterRequestDTO request = buildRequest();
            when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email already registered");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("✅ Role is set to USER automatically")
        void register_setsRoleUser() {
            RegisterRequestDTO request = buildRequest();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(mockUser);
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(jwtProvider.generateToken(any())).thenReturn("token");
            when(jwtProvider.getExpirationTime()).thenReturn(3600L);
            when(userMapper.toAuthResponseDTO(any())).thenReturn(mockAuthResponse);

            authService.register(request);

            assertThat(mockUser.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("✅ Password is encoded before saving")
        void register_encodesPassword() {
            RegisterRequestDTO request = buildRequest();

            when(userRepository.existsByEmail(anyString())).thenReturn(false);
            when(userMapper.toEntity(request)).thenReturn(mockUser);
            when(passwordEncoder.encode("password123")).thenReturn("$2a$hashed");
            when(userRepository.save(any(User.class))).thenReturn(mockUser);
            when(jwtProvider.generateToken(any())).thenReturn("token");
            when(jwtProvider.getExpirationTime()).thenReturn(3600L);
            when(userMapper.toAuthResponseDTO(any())).thenReturn(mockAuthResponse);

            authService.register(request);

            assertThat(mockUser.getPassword()).isEqualTo("$2a$hashed");
        }
    }

    // ══════════════════════════════════════════════
    // LOGIN
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("login()")
    class Login {

        private LoginRequestDTO buildRequest() {
            LoginRequestDTO req = new LoginRequestDTO();
            req.setEmail("test@example.com");
            req.setPassword("password123");
            return req;
        }

        @Test
        @DisplayName("✅ Valid credentials — returns token")
        void login_success() {
            LoginRequestDTO request = buildRequest();

            when(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                    .thenReturn(Optional.of(mockUser));
            when(jwtProvider.generateToken(mockUser)).thenReturn("mock-jwt-token");
            when(jwtProvider.getExpirationTime()).thenReturn(3600L);
            when(userMapper.toAuthResponseDTO(mockUser)).thenReturn(mockAuthResponse);

            AuthResponseDTO result = authService.login(request);

            assertThat(result).isNotNull();
            assertThat(result.getAccessToken()).isEqualTo("mock-jwt-token");

            verify(authenticationManager).authenticate(
                    argThat(token -> token instanceof UsernamePasswordAuthenticationToken
                            && token.getPrincipal().equals(request.getEmail())
                            && token.getCredentials().equals(request.getPassword()))
            );
        }

        @Test
        @DisplayName("❌ Wrong credentials — AuthenticationManager throws")
        void login_badCredentials_throwsException() {
            LoginRequestDTO request = buildRequest();

            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(authenticationManager).authenticate(any());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class);

            verify(userRepository, never()).findByEmailAndDeletedAtIsNull(anyString());
        }

        @Test
        @DisplayName("❌ User not found after auth — throws ResourceNotFoundException")
        void login_userNotFound_throwsException() {
            LoginRequestDTO request = buildRequest();

            when(userRepository.findByEmailAndDeletedAtIsNull(request.getEmail()))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(request.getEmail());
        }

        @Test
        @DisplayName("✅ authenticationManager is called with correct email and password")
        void login_callsAuthManagerWithCorrectCredentials() {
            LoginRequestDTO request = buildRequest();

            when(userRepository.findByEmailAndDeletedAtIsNull(anyString()))
                    .thenReturn(Optional.of(mockUser));
            when(jwtProvider.generateToken(any())).thenReturn("token");
            when(jwtProvider.getExpirationTime()).thenReturn(3600L);
            when(userMapper.toAuthResponseDTO(any())).thenReturn(mockAuthResponse);

            authService.login(request);

            ArgumentCaptor<UsernamePasswordAuthenticationToken> captor =
                    ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
            verify(authenticationManager).authenticate(captor.capture());

            assertThat(captor.getValue().getPrincipal()).isEqualTo("test@example.com");
            assertThat(captor.getValue().getCredentials()).isEqualTo("password123");
        }
    }

    // ══════════════════════════════════════════════
    // GOOGLE LOGIN
    // ══════════════════════════════════════════════
    @Nested
    @DisplayName("googleLogin()")
    class GoogleLogin {

        @Test
        @DisplayName("❌ Invalid Google token — throws IllegalArgumentException")
        void googleLogin_invalidToken_throwsException() {
            GoogleLoginRequestDTO request = new GoogleLoginRequestDTO();
            request.setIdToken("invalid-token");

            // The verifier will fail to verify, idToken == null triggers the exception
            assertThatThrownBy(() -> authService.googleLogin(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Failed to verify Google token");
        }

        @Test
        @DisplayName("❌ Null token — throws IllegalArgumentException")
        void googleLogin_nullToken_throwsException() {
            GoogleLoginRequestDTO request = new GoogleLoginRequestDTO();
            request.setIdToken(null);

            assertThatThrownBy(() -> authService.googleLogin(request))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        /**
         * Tests for existing / new user flow are done via an injectable verifier.
         * The tests below use a spy + partial mock approach to isolate business logic.
         */
        @Test
        @DisplayName("✅ Existing LOCAL user — authProvider updated to GOOGLE and token returned")
        void googleLogin_existingLocalUser_updatesProviderAndReturnsToken() throws Exception {
            // Arrange: mock verifier via a subclass spy
            GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
            when(payload.getEmail()).thenReturn("test@example.com");
            when(payload.get("given_name")).thenReturn("John");
            when(payload.get("family_name")).thenReturn("Doe");

            GoogleIdToken idToken = mock(GoogleIdToken.class);
            when(idToken.getPayload()).thenReturn(payload);

            GoogleIdTokenVerifier verifier = mock(GoogleIdTokenVerifier.class);
            when(verifier.verify("valid-google-token")).thenReturn(idToken);

            // Inject the mocked verifier via a testable subclass
            AuthServiceImpl spyService = spy(authService);
            doReturn(verifier).when(spyService).createGoogleVerifier();

            mockUser.setAuthProvider(AuthProvider.LOCAL);
            when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
                    .thenReturn(Optional.of(mockUser));
            when(userRepository.save(mockUser)).thenReturn(mockUser);
            when(jwtProvider.generateToken(mockUser)).thenReturn("google-jwt-token");
            when(jwtProvider.getExpirationTime()).thenReturn(3600L);
            when(userMapper.toAuthResponseDTO(mockUser)).thenReturn(mockAuthResponse);

            GoogleLoginRequestDTO request = new GoogleLoginRequestDTO();
            request.setIdToken("valid-google-token");

            AuthResponseDTO result = spyService.googleLogin(request);

            assertThat(result).isNotNull();
            assertThat(mockUser.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
            verify(userRepository).save(mockUser);
        }

        @Test
        @DisplayName("✅ Existing GOOGLE user — no re-save, token returned")
        void googleLogin_existingGoogleUser_noExtraSave() throws Exception {
            GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
            when(payload.getEmail()).thenReturn("test@example.com");
            when(payload.get("given_name")).thenReturn("John");
            when(payload.get("family_name")).thenReturn("Doe");

            GoogleIdToken idToken = mock(GoogleIdToken.class);
            when(idToken.getPayload()).thenReturn(payload);

            GoogleIdTokenVerifier verifier = mock(GoogleIdTokenVerifier.class);
            when(verifier.verify("valid-google-token")).thenReturn(idToken);

            AuthServiceImpl spyService = spy(authService);
            doReturn(verifier).when(spyService).createGoogleVerifier();

            mockUser.setAuthProvider(AuthProvider.GOOGLE);
            when(userRepository.findByEmailAndDeletedAtIsNull("test@example.com"))
                    .thenReturn(Optional.of(mockUser));
            when(jwtProvider.generateToken(mockUser)).thenReturn("google-jwt-token");
            when(jwtProvider.getExpirationTime()).thenReturn(3600L);
            when(userMapper.toAuthResponseDTO(mockUser)).thenReturn(mockAuthResponse);

            GoogleLoginRequestDTO request = new GoogleLoginRequestDTO();
            request.setIdToken("valid-google-token");

            spyService.googleLogin(request);

            // save() should NOT be called for an already-GOOGLE user
            verify(userRepository, never()).save(mockUser);
        }

        @Test
        @DisplayName("✅ New user via Google — created with correct fields")
        void googleLogin_newUser_createsUserAndReturnsToken() throws Exception {
            GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
            when(payload.getEmail()).thenReturn("newuser@gmail.com");
            when(payload.get("given_name")).thenReturn("Jane");
            when(payload.get("family_name")).thenReturn("Smith");

            GoogleIdToken idToken = mock(GoogleIdToken.class);
            when(idToken.getPayload()).thenReturn(payload);

            GoogleIdTokenVerifier verifier = mock(GoogleIdTokenVerifier.class);
            when(verifier.verify("valid-google-token")).thenReturn(idToken);

            AuthServiceImpl spyService = spy(authService);
            doReturn(verifier).when(spyService).createGoogleVerifier();

            when(userRepository.findByEmailAndDeletedAtIsNull("newuser@gmail.com"))
                    .thenReturn(Optional.empty());

            User savedUser = new User();
            savedUser.setEmail("newuser@gmail.com");
            savedUser.setFirstName("Jane");
            savedUser.setLastName("Smith");
            savedUser.setRole(Role.USER);
            savedUser.setAuthProvider(AuthProvider.GOOGLE);

            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtProvider.generateToken(savedUser)).thenReturn("new-user-token");
            when(jwtProvider.getExpirationTime()).thenReturn(3600L);
            when(userMapper.toAuthResponseDTO(savedUser)).thenReturn(mockAuthResponse);

            GoogleLoginRequestDTO request = new GoogleLoginRequestDTO();
            request.setIdToken("valid-google-token");

            AuthResponseDTO result = spyService.googleLogin(request);

            assertThat(result).isNotNull();

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User captured = captor.getValue();

            assertThat(captured.getEmail()).isEqualTo("newuser@gmail.com");
            assertThat(captured.getFirstName()).isEqualTo("Jane");
            assertThat(captured.getLastName()).isEqualTo("Smith");
            assertThat(captured.getRole()).isEqualTo(Role.USER);
            assertThat(captured.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(captured.getPassword()).isNull(); // No password for OAuth users
        }

        @Test
        @DisplayName("✅ Missing given_name in payload — defaults to 'User'")
        void googleLogin_missingGivenName_defaultsToUser() throws Exception {
            GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
            when(payload.getEmail()).thenReturn("noname@gmail.com");
            when(payload.get("given_name")).thenReturn(null);
            when(payload.get("family_name")).thenReturn(null);

            GoogleIdToken idToken = mock(GoogleIdToken.class);
            when(idToken.getPayload()).thenReturn(payload);

            GoogleIdTokenVerifier verifier = mock(GoogleIdTokenVerifier.class);
            when(verifier.verify("valid-token")).thenReturn(idToken);

            AuthServiceImpl spyService = spy(authService);
            doReturn(verifier).when(spyService).createGoogleVerifier();

            when(userRepository.findByEmailAndDeletedAtIsNull("noname@gmail.com"))
                    .thenReturn(Optional.empty());

            User savedUser = new User();
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(jwtProvider.generateToken(savedUser)).thenReturn("token");
            when(jwtProvider.getExpirationTime()).thenReturn(3600L);
            when(userMapper.toAuthResponseDTO(savedUser)).thenReturn(mockAuthResponse);

            GoogleLoginRequestDTO request = new GoogleLoginRequestDTO();
            request.setIdToken("valid-token");

            spyService.googleLogin(request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getFirstName()).isEqualTo("User");
            assertThat(captor.getValue().getLastName()).isEqualTo("User");
        }
    }
}
