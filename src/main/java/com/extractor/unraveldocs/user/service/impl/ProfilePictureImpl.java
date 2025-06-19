package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.userimpl.ProfilePictureService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.utils.imageupload.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfilePictureImpl implements ProfilePictureService {
    private final CloudinaryService cloudinaryService;
    private final ResponseBuilderService builderService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserResponse<String> uploadProfilePicture(User user, MultipartFile file) {
        String profilePictureUrl = cloudinaryService.uploadFile(
                file,
                CloudinaryService.getPROFILE_PICTURE_FOLDER(),
                file.getOriginalFilename(),
                CloudinaryService.getRESOURCE_TYPE_IMAGE()
        );

        user.setProfilePicture(profilePictureUrl);
        userRepository.save(user);

        return builderService
                .buildUserResponse(
                        profilePictureUrl,
                        HttpStatus.OK,
                        "Profile picture uploaded successfully."
                );
    }

    @Override
    @Transactional
    public UserResponse<Void> deleteProfilePicture(User user) {
        cloudinaryService.deleteFile(user.getProfilePicture());

        user.setProfilePicture(null);
        userRepository.save(user);

        return builderService.buildUserResponse(
                        null,
                        HttpStatus.OK,
                        "Profile picture deleted successfully."
                );
    }
}
