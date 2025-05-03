package com.extractor.unraveldocs.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Schema(description = "Email address of the user", example = "john-doe@test.com")
        String email,

        @NotBlank(message = "Email verification token is required")
        @Schema(description = "Email verification token", example = "1234567890abcdef")
        String emailVerificationToken
) {
}
