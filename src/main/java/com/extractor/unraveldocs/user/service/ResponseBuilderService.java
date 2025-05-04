package com.extractor.unraveldocs.user.service;

import com.extractor.unraveldocs.user.dto.UserData;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class ResponseBuilderService {
    public UserResponse buildUserResponse(User user) {
        UserData userData = new UserData();
        userData.setId(user.getId());
        userData.setProfilePicture(user.getProfilePicture());
        userData.setFirstName(user.getFirstName());
        userData.setLastName(user.getLastName());
        userData.setEmail(user.getEmail());
        userData.setRole(user.getRole());
        userData.setLastLogin(user.getLastLogin());
        userData.setVerified(user.isVerified());
        userData.setCreatedAt(user.getCreatedAt());
        userData.setUpdatedAt(user.getUpdatedAt());

        UserResponse userResponse = new UserResponse();
        userResponse.setStatusCode(HttpStatus.OK.value());
        userResponse.setStatus("success");
        userResponse.setMessage("User profile retrieved successfully");
        userResponse.setData(userData);

        return userResponse;
    }

    public UserResponse buildResponseWithoutData(String message) {
        UserResponse userResponse = new UserResponse();
        userResponse.setStatusCode(HttpStatus.OK.value());
        userResponse.setStatus("success");
        userResponse.setMessage(message);
        userResponse.setData(null);

        return userResponse;
    }
}
