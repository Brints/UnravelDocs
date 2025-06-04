package com.extractor.unraveldocs.user.controller;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.user.dto.UserData;
import com.extractor.unraveldocs.user.dto.request.ChangePasswordDto;
import com.extractor.unraveldocs.user.dto.request.ForgotPasswordDto;
import com.extractor.unraveldocs.user.dto.request.ProfileUpdateRequestDto;
import com.extractor.unraveldocs.user.dto.request.ResetPasswordDto;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.PasswordResetParams;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
    private final UserRepository userRepository;

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
            throw new ForbiddenException("Please login to view your profile");
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
    @PreAuthorize("hasRole('ROLE_admin')")
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

    /**
     * Change password implementation to update the user's password.
     *
     * @param changePasswordDto The DTO containing the current and new passwords.
     * @return ResponseEntity indicating the result of the operation.
     */
    @Operation(summary = "Change password")
    @PostMapping("/change-password")
    public ResponseEntity<UserResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails authenticatedUser,
            @Valid @RequestBody ChangePasswordDto changePasswordDto
    ) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("Please login to change your password");
        }
        return ResponseEntity.ok(userService.changePassword(
                new PasswordResetParams(authenticatedUser.getUsername(),
                        null)
                , changePasswordDto));
    }

    /**
     * Update the profile of the currently authenticated user.
     *
     * @param request The request containing the updated profile information.
     * @return ResponseEntity indicating the result of the operation.
     */
    @Operation(summary = "Update user profile",
            description = "User can update some information in their profile.",
            responses = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            )
            }
    )
    @PutMapping(value = "/update-profile",
            consumes = {
                    MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_JSON_VALUE
            }
    )
    public ResponseEntity<UserResponse<UserData>> updateProfile(
            @AuthenticationPrincipal UserDetails authenticatedUser,
            @Valid @ModelAttribute ProfileUpdateRequestDto request
    ) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("Please login to update your profile");
        }

        if (request == null) {
            throw new BadRequestException("Request body cannot be null");
        }

        String userEmail = authenticatedUser.getUsername();
        String userId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ForbiddenException("User not found"))
                .getId();

        return ResponseEntity.ok(userService.updateProfile(request, userId));
    }

    /**
     * Delete the profile of the currently authenticated user.
     *
     * @param authenticatedUser The authenticated user whose profile is to be deleted.
     * @return ResponseEntity indicating the result of the operation.
     */
    @Operation(summary = "Delete user profile")
    @DeleteMapping("/delete-account")
    public ResponseEntity<?> deleteUser(
            @AuthenticationPrincipal UserDetails authenticatedUser
    ) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("Please login to delete your profile");
        }

        String userId = userRepository.findByEmail(authenticatedUser.getUsername())
                .orElseThrow(() -> new ForbiddenException("User not found"))
                .getId();

        userService.deleteUser(userId);
        return ResponseEntity.ok("User profile deleted successfully");
    }
}
