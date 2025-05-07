package com.extractor.unraveldocs.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

@Builder
@Schema(
        description = "Update Profile Request",
        title = "Update Profile Request")
public record ProfileUpdateRequestDto(
        @Schema(description = "First Name of the User", example = "John")
        @Size(min = 2, max = 80, message = "First name must be between 2 and 80 characters.")
        String firstName,

        @Schema(description = "Last Name of the User", example = "Doe")
        @Size(min = 2, max = 80, message = "Last name must be between 2 and 80 characters.")
        String lastName,

        @Schema(description = "Profile Picture URL of the User")
        MultipartFile profilePicture
) {
        @Override
        @SuppressWarnings("NullableProblems")
        public String toString() {
                return "UpdateProfileRequestDto{" +
                        "firstName='" + firstName + '\'' +
                        ", lastName='" + lastName + '\'' +
                        ", profilePicture=" + (profilePicture != null ? "[FILE]" : "null") +
                        '}';
        }
}
