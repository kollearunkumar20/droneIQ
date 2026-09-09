package com.droneiq.auth.service;

import com.droneiq.auth.dto.LoginRequest;
import com.droneiq.auth.dto.LoginResponse;
import com.droneiq.auth.dto.RegisterRequest;
import com.droneiq.auth.dto.UserResponse;
import com.droneiq.auth.entity.Role;
import com.droneiq.auth.entity.User;
import com.droneiq.auth.repository.UserRepository;
import com.droneiq.common.exception.DuplicateResourceException;
import com.droneiq.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthenticationManager authenticationManager;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);
        authenticationManager = mock(AuthenticationManager.class);

        authService = new AuthService(userRepository, passwordEncoder, jwtService, authenticationManager);
    }

    private User sampleUser() {
        return User.builder()
                .id(1L)
                .username("admin")
                .email("admin@droneiq.io")
                .passwordHash("hashed-password")
                .role(Role.ADMIN)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    @Test
    @DisplayName("Should successfully authenticate user and return token")
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("admin", "admin123");
        User user = sampleUser();

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock-jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.token());
        assertEquals("admin", response.username());
        assertEquals("ADMIN", response.role());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should propagate BadCredentialsException on invalid credentials")
    void testLoginInvalidCredentials() {
        LoginRequest request = new LoginRequest("admin", "wrong-password");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    @DisplayName("Should register new user successfully")
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("operator1", "op@droneiq.io", "secret123", Role.OPERATOR);

        when(userRepository.existsByUsername("operator1")).thenReturn(false);
        when(userRepository.existsByEmail("op@droneiq.io")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded-secret");
        when(userRepository.save(any(User.class))).thenAnswer(i -> {
            User u = i.getArgument(0);
            u.setId(2L);
            u.setCreatedAt(Instant.now());
            return u;
        });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("operator1", response.username());
        assertEquals("op@droneiq.io", response.email());
        assertEquals(Role.OPERATOR, response.role());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException on duplicate username")
    void testRegisterDuplicateUsername() {
        RegisterRequest request = new RegisterRequest("admin", "admin2@droneiq.io", "secret123", Role.OPERATOR);
        when(userRepository.existsByUsername("admin")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> authService.register(request));
    }

    @Test
    @DisplayName("Should get current user details")
    void testGetCurrentUser() {
        User user = sampleUser();
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserResponse response = authService.getCurrentUser("admin");
        assertEquals("admin", response.username());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user does not exist")
    void testGetCurrentUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser("unknown"));
    }
}
