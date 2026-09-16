package com.droneiq.auth.controller;

import com.droneiq.auth.dto.InviteUserRequest;
import com.droneiq.auth.dto.UpdateRoleRequest;
import com.droneiq.auth.dto.UserResponse;
import com.droneiq.auth.service.AuthService;
import com.droneiq.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "RBAC System Config & User Management Endpoints")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final AuthService authService;

    public UserController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER:READ')")
    @Operation(summary = "List all registered users (Super Admin & Fleet Manager)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = authService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/invite")
    @PreAuthorize("hasAuthority('USER:INVITE')")
    @Operation(summary = "Invite/Create user within role bounds (Super Admin or Fleet Manager)")
    public ResponseEntity<ApiResponse<UserResponse>> inviteUser(
            @Valid @RequestBody InviteUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = authService.inviteUser(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User invited successfully", response));
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasAuthority('USER:MANAGE')")
    @Operation(summary = "Update user role (Super Admin only)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateRoleRequest request) {
        UserResponse response = authService.updateUserRole(userId, request.role());
        return ResponseEntity.ok(ApiResponse.success("User role updated successfully", response));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER:MANAGE')")
    @Operation(summary = "Delete user account (Super Admin only)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User deleted successfully", null));
    }
}
