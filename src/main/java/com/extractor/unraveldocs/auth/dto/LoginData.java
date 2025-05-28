package com.extractor.unraveldocs.auth.dto;

import com.extractor.unraveldocs.auth.enums.Role;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LoginData(
        String id,
        String firstName,
        String lastName,
        String email,
        Role role,
        LocalDateTime lastLogin,
        boolean isActive,
        boolean isVerified,
        String accessToken,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
