package com.extractor.unraveldocs.auth.dto.request;

import com.extractor.unraveldocs.auth.dto.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
@PasswordMatches
public record SignUpRequestDto(
        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 80, message = "First name must be between 2 and 80 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 80, message = "Last name must be between 2 and 80 characters")
        String lastName,

        @NotBlank(message = "Email is required")
        @Email(message = "Please enter a valid email address")
        @Size(max = 100, message = "Email must be less than 100 characters")
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        @Pattern.List({
                @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter"),
                @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter"),
                @Pattern(regexp = ".*[0-9].*", message = "Password must contain at least one digit"),
                @Pattern(regexp = ".*[@$!%*?&].*", message = "Password must contain at least one special character")
        })
        String password,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
        @Override
        public String toString() {
                return "SignUpRequestDto{" +
                        "firstName='" + firstName + '\'' +
                        ", lastName='" + lastName + '\'' +
                        ", email='" + email + '\'' +
                        ", password='[REDACTED]'" +
                        ", confirmPassword='[REDACTED]'" +
                        '}';
        }
}
