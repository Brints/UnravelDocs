package com.extractor.unraveldocs.auth.dto;

import com.extractor.unraveldocs.auth.enums.Role;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SignupUserData(
        String id,
        String profilePicture,
        String firstName,
        String lastName,
        String email,
        Role role,
        LocalDateTime lastLogin,
        boolean isActive,
        boolean isVerified
) {
}
