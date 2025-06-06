package com.extractor.unraveldocs.loginattempts.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LoginAttemptsDto(
        @NotBlank(message = "Login attempts ID cannot be blank")
        int loginAttempts,

        @NotBlank(message = "Blocked status cannot be blank")
        boolean isBlocked,

        @NotBlank(message = "Blocked until time cannot be blank")
        LocalDateTime blockedUntil
) {
}
