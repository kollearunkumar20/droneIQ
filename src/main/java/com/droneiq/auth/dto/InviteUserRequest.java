package com.droneiq.auth.dto;

import com.droneiq.auth.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InviteUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
        String username,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotBlank(message = "Temporary password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String temporaryPassword,

        @NotNull(message = "Role is required")
        Role role
) {
}
