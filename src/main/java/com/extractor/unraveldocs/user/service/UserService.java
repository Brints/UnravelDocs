package com.extractor.unraveldocs.user.service;

import com.extractor.unraveldocs.user.dto.UserData;
import com.extractor.unraveldocs.user.dto.request.*;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.IPasswordReset;
import com.extractor.unraveldocs.user.interfaces.userimpl.*;
import com.extractor.unraveldocs.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final ChangePasswordService changePasswordService;
    private final DeleteUserService deleteUserService;
    private final GetUserProfileService getUserProfileService;
    private final PasswordResetService passwordResetService;
    private final ProfileUpdateService profileUpdateService;
    private final ProfilePictureService profilePictureService;


    public UserResponse<UserData> getUserProfileByOwner(String userId) {
        return getUserProfileService.getUserProfileByOwner(userId);
    }

    public UserResponse<Void> forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        return passwordResetService.forgotPassword(forgotPasswordDto);
    }

    public UserResponse<Void> resetPassword(IPasswordReset params, ResetPasswordDto request) {
        return passwordResetService.resetPassword(params, request);
    }

    public UserResponse<Void> changePassword(ChangePasswordDto request) {
        return changePasswordService.changePassword(request);
    }

    public UserResponse<UserData> updateProfile(ProfileUpdateRequestDto request, String userId) {
        return profileUpdateService.updateProfile(request, userId);
    }

    public void deleteUser(String userId) {
        deleteUserService.deleteUser(userId);
    }

    public UserResponse<String> uploadProfilePicture(User user, MultipartFile file) {
        return profilePictureService.uploadProfilePicture(user, file);
    }

    public UserResponse<Void> deleteProfilePicture(User user) {
        return profilePictureService.deleteProfilePicture(user);
    }
}
