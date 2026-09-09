package com.droneiq.auth.dto;

public record LoginResponse(
    String token,
    String tokenType,
    Long expiresIn,
    String username,
    String email,
    String role
) {
    public LoginResponse(String token, Long expiresIn, String username, String email, String role) {
        this(token, "Bearer", expiresIn, username, email, role);
    }
}
