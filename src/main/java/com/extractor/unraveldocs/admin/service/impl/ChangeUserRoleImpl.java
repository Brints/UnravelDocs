package com.extractor.unraveldocs.admin.service.impl;

import com.extractor.unraveldocs.admin.dto.AdminData;
import com.extractor.unraveldocs.admin.dto.request.ChangeRoleDto;
import com.extractor.unraveldocs.admin.interfaces.ChangeUserRoleService;
import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.exceptions.custom.UnauthorizedException;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeUserRoleImpl implements ChangeUserRoleService {
    private final UserRepository userRepository;
    private final ResponseBuilderService responseBuilder;

    @Override
    public UserResponse<AdminData> changeUserRole(ChangeRoleDto request, Authentication authentication) {
        String userId = request.getUserId();
        System.out.println("Changing role for user ID: " + userId);

        // Admin or Super Admin check
        if (authentication.getAuthorities().stream()
                .noneMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_admin") ||
                        grantedAuthority.getAuthority().equals("ROLE_super_admin"))) {
            throw new UnauthorizedException("You must be an admin or super admin to change user roles");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        if (!user.isVerified()) {
            throw new ForbiddenException("Cannot change role for unverified users");
        }

        user.setRole(request.getRole());
        userRepository.save(user);


        AdminData data = getResponseData(user);

        return responseBuilder.buildUserResponse(
                data,
                HttpStatus.OK,
                "User role changed successfully."
        );
    }

    private static AdminData getResponseData(User user) {
        AdminData data = new AdminData();
        data.setId(user.getId());
        data.setProfilePicture(user.getProfilePicture());
        data.setFirstName(user.getFirstName());
        data.setLastName(user.getLastName());
        data.setEmail(user.getEmail());
        data.setRole(user.getRole());
        data.setLastLogin(user.getLastLogin());
        data.setVerified(user.isVerified());
        data.setCreatedAt(user.getCreatedAt());
        data.setUpdatedAt(user.getUpdatedAt());
        return data;
    }
}
