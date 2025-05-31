package com.extractor.unraveldocs.admin.controller;

import com.extractor.unraveldocs.admin.dto.AdminData;
import com.extractor.unraveldocs.admin.dto.request.ChangeRoleDto;
import com.extractor.unraveldocs.admin.dto.request.UserFilterDto;
import com.extractor.unraveldocs.admin.dto.response.UserListData;
import com.extractor.unraveldocs.admin.service.AdminService;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.global.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin endpoints for user management and other administrative tasks")
public class AdminController {
    private final AdminService adminService;

    /**
     * Change the role of a user to ADMIN or MODERATOR.
     *
     * @param authenticatedUser The currently authenticated user.
     * @param request           The request containing the user ID and new role.
     * @return ResponseEntity containing the result of the operation.
     */
    @Operation(
            summary = "Change user role to ADMIN or MODERATOR",
            description = "Allows an admin to change the role of a user to ADMIN or MODERATOR.")
    @PutMapping("/change-role")
    public ResponseEntity<UserResponse<AdminData>> changeUserRole(Authentication authenticatedUser, @RequestBody ChangeRoleDto request) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("You must be logged in to change user roles");
        }

        UserResponse<AdminData> response = adminService.changeUserRole(request, authenticatedUser);

        return ResponseEntity.ok(response);
    }

    /**
     * Get a paginated list of all users with optional filtering and sorting.
     *
     * @param request         The request containing filter and pagination parameters.
     * @param authentication  The current user's authentication details.
     * @return ResponseEntity containing the list of users and pagination details.
     */
    @Operation(
            summary = "Get all users",
            description = "Fetches a paginated list of all users with optional filtering and sorting.")
    @GetMapping("/users")
    public ResponseEntity<UserResponse<UserListData>> getAllUsers(
            @Valid @ModelAttribute UserFilterDto request,
            Authentication authentication) {

        if (authentication == null) {
            throw new ForbiddenException("You must be logged in to view users");
        }

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_admin") || auth.getAuthority().equals(
                        "ROLE_super_admin"));
        if (!isAdmin) {
            throw new ForbiddenException("Only admins can view users");
        }

        UserResponse<UserListData> response = adminService.getAllUsers(request);

        return ResponseEntity.ok(response);
    }
}
