package com.extractor.unraveldocs.auth.service;

import com.extractor.unraveldocs.auth.dto.LoginUserData;
import com.extractor.unraveldocs.auth.dto.SignupUserData;
import com.extractor.unraveldocs.auth.dto.response.SignupUserResponse;
import com.extractor.unraveldocs.auth.dto.response.UserLoginResponse;
import com.extractor.unraveldocs.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class AuthResponseBuilderService {
    public SignupUserResponse buildUserSignupResponse(User user) {
        return SignupUserResponse.builder()
                .statusCode(HttpStatus.CREATED.value())
                .status("success")
                .message("User registered successfully")
                .data(SignupUserData.builder()
                        .id(user.getId())
                        .profilePicture(user.getProfilePicture())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .isVerified(user.isVerified())
                        .isActive(user.isActive())
                        .role(user.getRole())
                        .lastLogin(user.getLastLogin())
                        .build())
                .build();
    }

    public UserLoginResponse buildUserLoginResponse(User user, String accessToken) {
        return UserLoginResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .status("success")
                .message("User logged in successfully")
                .data(LoginUserData.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .isVerified(user.isVerified())
                        .isActive(user.isActive())
                        .role(user.getRole())
                        .lastLogin(user.getLastLogin())
                        .accessToken(accessToken)
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build())
                .build();
    }
}
