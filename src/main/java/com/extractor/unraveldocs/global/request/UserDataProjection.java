package com.extractor.unraveldocs.global.request;

import com.extractor.unraveldocs.auth.enums.Role;

import java.time.LocalDateTime;

public interface UserDataProjection {
    void setId(String id);
    void setProfilePicture(String profilePicture);
    void setFirstName(String firstName);
    void setLastName(String lastName);
    void setEmail(String email);
    void setLastLogin(LocalDateTime lastLogin);
    void setRole(Role role);
    void setVerified(boolean verified);
    void setCreatedAt(LocalDateTime createdAt);
    void setUpdatedAt(LocalDateTime updatedAt);
}
