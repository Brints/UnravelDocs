package com.extractor.unraveldocs.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
public record GeneratePasswordDto(
        @NotBlank(message = "Provide a valid length as a string")
        String passwordLength,

        String excludedChars
) {
}
