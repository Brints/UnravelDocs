package com.extractor.unraveldocs.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * DTO for resending email verification.
 * <p>
 * This class is used to transfer data for the request to resend email verification.
 * It contains the email address of the user who wants to resend the verification email.
 */
@Builder
public record ResendEmailVerificationDto(
        @NotBlank(message = "Email cannot be empty.")
        @Email(message = "Email should be valid.")
        @Schema(description = "Email address of the user", example = "john-doe@test.com")
        String email
) {
}
