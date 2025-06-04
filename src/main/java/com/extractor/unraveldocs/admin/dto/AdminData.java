package com.extractor.unraveldocs.admin.dto;

import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.global.request.UserDataProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminData implements UserDataProjection {
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
