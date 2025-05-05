package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.user.dto.request.ProfileUpdateRequestDto;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.userimpl.ProfileUpdateService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.user.service.ResponseBuilderService;
import com.extractor.unraveldocs.utils.imageupload.aws.AwsS3Service;
import com.extractor.unraveldocs.utils.userlib.UserLibrary;
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
    private final UserLibrary userLibrary;

    @Override
    @Transactional
    public UserResponse updateProfile(ProfileUpdateRequestDto request, String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with ID: " + userId));

        String updatedFirstName = request.firstName() != null || !request.firstName().isEmpty() ? request.firstName() : user.getFirstName();
        String updatedLastName = request.lastName() != null || !request.firstName().isEmpty() ? request.lastName() : user.getLastName();

        String currentProfilePictureUrl = user.getProfilePicture();
        if (request.profilePicture() != null && !request.profilePicture().isEmpty()) {

            String fileName = awsS3Service.generateFileName(request.profilePicture().getOriginalFilename());
            currentProfilePictureUrl = awsS3Service.uploadFile(request.profilePicture(), fileName);

            if (currentProfilePictureUrl != null && !currentProfilePictureUrl.isEmpty()) {
                awsS3Service.deleteFile(currentProfilePictureUrl);
            }
        }

        user.setFirstName(userLibrary.capitalizeFirstLetterOfName(updatedFirstName));
        user.setLastName(userLibrary.capitalizeFirstLetterOfName(updatedLastName));
        user.setProfilePicture(currentProfilePictureUrl);
        userRepository.save(user);

        return responseBuilder.buildUserResponse(user, "Profile updated successfully");
    }
}
