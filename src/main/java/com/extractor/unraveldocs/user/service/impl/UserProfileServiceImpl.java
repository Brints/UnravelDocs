package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.userimpl.UserProfileService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.user.service.ResponseBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserRepository userRepository;
    private final ResponseBuilderService responseBuilder;

    @Override
    public UserResponse getUserProfileById(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return responseBuilder.buildUserResponse(user, "User profile retrieved successfully");
    }

    @Override
    public UserResponse getAuthenticatedUserProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return responseBuilder.buildUserResponse(user, "User profile retrieved successfully");
    }
}
