package com.extractor.unraveldocs.auth.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record UserData(
        String id,
        String firstName,
        String lastName,
        String email,
        LocalDateTime lastLogin,
        boolean isActive,
        boolean isVerified,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
