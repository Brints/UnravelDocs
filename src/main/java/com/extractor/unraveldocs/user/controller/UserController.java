package com.extractor.unraveldocs.user.controller;

import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.user.dto.request.ForgotPasswordDto;
import com.extractor.unraveldocs.user.dto.request.ResetPasswordDto;
import com.extractor.unraveldocs.user.interfaces.PasswordResetParams;
import com.extractor.unraveldocs.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Manage user profiles and settings")
public class UserController {
    private final UserService userService;

    /**
     * Get the profile of the currently authenticated user.
     *
     * @return ResponseEntity containing the user profile information.
     */
    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUserProfile(
            @AuthenticationPrincipal UserDetails authenticatedUser
    ) {

        if (authenticatedUser == null) {
            throw new ForbiddenException("User not authenticated");
        }

        return ResponseEntity.ok(userService.getAuthenticatedUserProfile(authenticatedUser.getUsername()));
    }

    /**
     * Get the profile of a user by their ID.
     *
     * @param userId The ID of the user whose profile is to be retrieved.
     * @return ResponseEntity containing the user profile information.
     */
    @Operation(summary = "Get user profile by ID")
    @GetMapping("/{userId}")
    @PreAuthorize("hasPermission(#userId, 'VIEW_PROFILE') or hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUserProfileById(
            @Parameter(description = "ID of the user to retrieve")
            @PathVariable String userId
    ) {
        return ResponseEntity.ok(userService.getUserProfileById(userId));
    }

    /**
     * Forgot password implementation to send a password reset link to the user's email.
     *
     * @param forgotPasswordDto The DTO containing the email address of the user requesting a password reset.
     * @return ResponseEntity indicating the result of the operation.
     */
    @Operation(summary = "Forgot password")
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordDto forgotPasswordDto
            ) {

        return ResponseEntity.ok(userService.forgotPassword(forgotPasswordDto));
    }

    /**
     * Reset password implementation to update the user's password.
     *
     * @param resetPasswordDto The DTO containing the new password and confirmation.
     * @return ResponseEntity indicating the result of the operation.
     */
    @Operation(summary = "Reset password")
    @PostMapping("/reset-password/{token}/{email}")
    public ResponseEntity<?> resetPassword(
            @Parameter(description = "Password reset token")
            @PathVariable String token,

            @Parameter(description = "Email address of the user")
            @PathVariable String email,

            @Parameter(description = "New password and confirmation")
            @Valid @RequestBody ResetPasswordDto resetPasswordDto
            ) {
        return ResponseEntity.ok(userService.resetPassword(new PasswordResetParams(email, token), resetPasswordDto));
    }
}
