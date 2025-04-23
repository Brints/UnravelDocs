package com.extractor.unraveldocs.user.controller;

import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.user.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
    private final UserRepository userRepository;

    /**
     * Get the profile of the currently authenticated user.
     *
     * @return ResponseEntity containing the user profile information.
     */
    @GetMapping("/me/{userId}")
    public ResponseEntity<?> getUserProfile(
            @PathVariable("userId") String userId,
            @AuthenticationPrincipal UserDetails authenticatedUser
    ) {
        if (userId == null || userId.isEmpty()) {
            throw new ForbiddenException("User ID cannot be null or empty");
        }

        if (authenticatedUser == null) {
            throw new ForbiddenException("User not authenticated");
        }

        return ResponseEntity.ok(userService.userProfile(userId, authenticatedUser));
    }
}
