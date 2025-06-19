package com.extractor.unraveldocs.auth.dto.request;

import com.extractor.unraveldocs.auth.dto.PasswordMatches;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
@Schema(description = "New User Sign Up Request DTO")
@PasswordMatches
public record SignUpRequestDto(
        @Schema(description = "First name of the user", example = "John")
        @NotNull(message = "First name is required")
        @Size(min = 2, max = 80, message = "First name must be between 2 and 80 characters")
        String firstName,

        @Schema(description = "Last name of the user", example = "Doe")
        @NotNull(message = "Last name is required")
        @Size(min = 2, max = 80, message = "Last name must be between 2 and 80 characters")
        String lastName,

        @Schema(description = "Email address of the user", example = "johndoe@example.com")
        @NotNull(message = "Email is required")
        @Email(message = "Please enter a valid email address")
        @Pattern(
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
                message = "Email must be a valid email address format"
        )
        @Size(max = 100, message = "Email must be less than 100 characters")
        String email,

        @Schema(description = "Password of the user", example = "P@ssw0rd123")
        @NotNull(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern.List({
                @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter"),
                @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter"),
                @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one digit"),
                @Pattern(regexp = ".*[@$!%*?&].*", message = "Password must contain at least one special character")
        })
        String password,

        @Schema(description = "Confirm password of the user", example = "P@ssw0rd123")
        @NotNull(message = "Confirm password is required")
        String confirmPassword
) {
        @Override
        @SuppressWarnings("NullableProblems")
        public String toString() {
                return "SignUpRequestDto{" +
                        "firstName='" + firstName + '\'' +
                        ", lastName='" + lastName + '\'' +
                        ", email='" + (email != null ? "[REDACTED]" : "null") + '\'' +
                        ", password='[REDACTED]'" +
                        ", confirmPassword='[REDACTED]'" +
                        '}';
        }
}
