package com.extractor.unraveldocs.user.dto;

import com.extractor.unraveldocs.auth.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
