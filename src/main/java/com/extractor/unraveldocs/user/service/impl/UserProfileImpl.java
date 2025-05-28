package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.dto.UserData;
import com.extractor.unraveldocs.user.interfaces.userimpl.UserProfileService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileImpl implements UserProfileService {
    private final UserRepository userRepository;
    private final ResponseBuilderService responseBuilder;

    @Override
    public UserResponse<UserData> getUserProfileById(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserData data = getUserData(user);

        return responseBuilder.buildUserResponse(
                data,
                HttpStatus.OK,
                "User profile retrieved successfully"
        );
    }

    @Override
    public UserResponse<UserData> getAuthenticatedUserProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserData data = getUserData(user);

        return responseBuilder.buildUserResponse(
                data,
                HttpStatus.OK,
                "User profile retrieved successfully"
        );
    }

    private static UserData getUserData(User user) {
        UserData data = new UserData();
        data.setId(user.getId());
        data.setProfilePicture(user.getProfilePicture());
        data.setFirstName(user.getFirstName());
        data.setLastName(user.getLastName());
        data.setEmail(user.getEmail());
        data.setLastLogin(user.getLastLogin());
        data.setRole(user.getRole());
        data.setVerified(user.isVerified());
        data.setCreatedAt(user.getCreatedAt());
        data.setUpdatedAt(user.getUpdatedAt());
        return data;
    }
}
