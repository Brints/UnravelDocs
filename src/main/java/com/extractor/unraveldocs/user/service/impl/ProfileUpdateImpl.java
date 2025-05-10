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

import java.util.Optional;

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
        Optional<User> optionalUser = userRepository.findById(userId);

        if (optionalUser.isEmpty()) {
            throw new NotFoundException("User not found with ID: " + userId);
        }

        User user = optionalUser.get();

        if (request.firstName() != null && !request.firstName().isEmpty() && !request.firstName().equalsIgnoreCase(user.getFirstName())) {
            String capitalizedFirstName = userLibrary.capitalizeFirstLetterOfName(request.firstName());
            user.setFirstName(capitalizedFirstName);
        }

        if (request.lastName() != null && !request.lastName().isEmpty() && !request.lastName().equalsIgnoreCase(user.getLastName())) {
            String capitalizedLastName = userLibrary.capitalizeFirstLetterOfName(request.lastName());
            user.setLastName(capitalizedLastName);
        }

        String newProfilePictureUrl;
        if (request.profilePicture() != null && !request.profilePicture().isEmpty()) {

            if (user.getProfilePicture() != null) {
                awsS3Service.deleteFile(user.getProfilePicture());
            }

            String fileName = awsS3Service.generateFileName(request.profilePicture().getOriginalFilename());
            newProfilePictureUrl = awsS3Service.uploadFile(request.profilePicture(), fileName);
            user.setProfilePicture(newProfilePictureUrl);
        }

        User updatedUser = userRepository.save(user);

        return responseBuilder.buildUserResponse(updatedUser, "Profile updated successfully");
    }
}
