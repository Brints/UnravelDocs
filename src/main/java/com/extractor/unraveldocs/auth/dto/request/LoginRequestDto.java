package com.extractor.unraveldocs.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDto(
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Schema(description = "Email address of the user", example = "john-doe@test.com")
        String email,

        @NotBlank(message = "Password is required")
        @Schema(description = "Password of the registered user", example = "P@ssw0rd123")
        String password
) {
}
