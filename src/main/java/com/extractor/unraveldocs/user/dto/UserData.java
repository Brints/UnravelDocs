package com.extractor.unraveldocs.user.dto;

import com.extractor.unraveldocs.auth.enums.Role;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserData {
    String id;
    String profilePicture;
    String firstName;
    String lastName;
    String email;
    Role role;
    LocalDateTime lastLogin;
    boolean isVerified;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
