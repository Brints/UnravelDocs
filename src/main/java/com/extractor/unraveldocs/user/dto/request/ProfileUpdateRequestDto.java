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
        String firstName,

        @Schema(description = "Last Name of the User", example = "Doe")
        String lastName,

        @Schema(description = "Profile Picture URL of the User")
        MultipartFile profilePicture
) {
}
