package com.extractor.unraveldocs.auth.dto;

import com.extractor.unraveldocs.auth.enums.Role;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record LoginData(
        String id,
        String firstName,
        String lastName,
        String email,
        Role role,
        OffsetDateTime lastLogin,
        boolean isActive,
        boolean isVerified,
        String accessToken,
        String refreshToken,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}
