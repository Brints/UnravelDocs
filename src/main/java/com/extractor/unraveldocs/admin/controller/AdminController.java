package com.extractor.unraveldocs.admin.controller;

import com.extractor.unraveldocs.admin.dto.AdminData;
import com.extractor.unraveldocs.admin.dto.request.ChangeRoleDto;
import com.extractor.unraveldocs.admin.service.AdminService;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.global.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<UserResponse<AdminData>> changeUserRole(Authentication authenticatedUser,
                                                                  @RequestBody ChangeRoleDto request) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("You must be logged in to change user roles");
        }

        UserResponse<AdminData> response = adminService.changeUserRole(request, authenticatedUser);

        return ResponseEntity.ok(response);
    }
}
