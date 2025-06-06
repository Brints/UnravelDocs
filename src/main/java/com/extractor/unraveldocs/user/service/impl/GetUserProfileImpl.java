package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.dto.UserData;
import com.extractor.unraveldocs.user.interfaces.userimpl.GetUserProfileService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import static com.extractor.unraveldocs.global.response.ResponseData.getResponseData;

@Service
@RequiredArgsConstructor
public class GetUserProfileImpl implements GetUserProfileService {
    private final UserRepository userRepository;
    private final ResponseBuilderService responseBuilder;

    @Override
    @Cacheable(value = "getProfileByAdmin", key = "#userId")
    public UserResponse<UserData> getUserProfileByAdmin(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserData data = getResponseData(user, UserData::new);

        return responseBuilder.buildUserResponse(
                data,
                HttpStatus.OK,
                "User profile retrieved successfully"
        );
    }

    @Override
    @Cacheable(value = "getProfileByUser", key = "#userId")
    public UserResponse<UserData> getUserProfileByOwner(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        UserData data = getResponseData(user, UserData::new);

        return responseBuilder.buildUserResponse(
                data,
                HttpStatus.OK,
                "User profile retrieved successfully"
        );
    }
}
