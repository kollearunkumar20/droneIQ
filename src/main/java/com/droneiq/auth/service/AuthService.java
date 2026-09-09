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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .or(() -> userRepository.findByEmail(request.username()))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.username()));

        String token = jwtService.generateToken(user);
        return new LoginResponse(
                token,
                jwtService.getExpirationMs(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateResourceException("Username already exists: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role() != null ? request.role() : Role.OPERATOR)
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.fromEntity(savedUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return UserResponse.fromEntity(user);
    }
}
