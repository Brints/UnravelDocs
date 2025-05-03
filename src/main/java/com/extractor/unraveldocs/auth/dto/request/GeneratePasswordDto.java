package com.extractor.unraveldocs.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record GeneratePasswordDto(
        @NotBlank(message = "Provide a valid length as a string")
        @Schema(description = "Length of the password to be generated", example = "12")
        String passwordLength,

        @Schema(description = "Characters that you wish to exclude from the password", example = "^=Av3r@ge")
        String excludedChars
) {
}
