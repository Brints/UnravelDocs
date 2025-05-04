package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.user.dto.request.ProfileUpdateRequestDto;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.userimpl.ProfileUpdateService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.user.service.ResponseBuilderService;
import com.extractor.unraveldocs.utils.imageupload.aws.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileUpdateImpl implements ProfileUpdateService {
    private final UserRepository userRepository;
    private final AwsS3Service awsS3Service;
    private final ResponseBuilderService responseBuilder;

    @Override
    @Transactional
    public UserResponse updateProfile(ProfileUpdateRequestDto request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        String updatedFirstName = request.firstName() != null ? request.firstName() : user.getFirstName();
        String updatedLastName = request.lastName() != null ? request.lastName() : user.getLastName();

        String updatedProfilePictureUrl = null;
        if (request.profilePicture() != null && !request.profilePicture().isEmpty()) {
            String fileName = "profile_pictures/" + UUID.randomUUID() + "-" + request.profilePicture().getOriginalFilename();
            updatedProfilePictureUrl = awsS3Service.uploadFile(request.profilePicture(), fileName);

            String oldProfilePictureUrl = user.getProfilePicture();
            if (oldProfilePictureUrl != null && !oldProfilePictureUrl.isEmpty()) {
                awsS3Service.deleteFile(oldProfilePictureUrl);
            }
        }

        user.setFirstName(updatedFirstName);
        user.setLastName(updatedLastName);
        user.setProfilePicture(updatedProfilePictureUrl);
        userRepository.save(user);

        return responseBuilder.buildUserResponse(user, "Profile updated successfully");
    }
}
