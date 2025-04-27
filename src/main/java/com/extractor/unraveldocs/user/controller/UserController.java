package com.extractor.unraveldocs.user.controller;

import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
