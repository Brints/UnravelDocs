package com.extractor.unraveldocs.user.dto.response;

import com.extractor.unraveldocs.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileData {
    private String id;
    private String profilePicture;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private LocalDateTime lastLogin;
    private boolean isVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
