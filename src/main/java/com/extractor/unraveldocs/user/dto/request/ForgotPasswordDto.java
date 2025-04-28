package com.extractor.unraveldocs.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record ForgotPasswordDto(
        @NotBlank
        @Email
        String email
) {
}
