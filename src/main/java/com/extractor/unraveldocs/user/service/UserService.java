package com.extractor.unraveldocs.user.service;

import com.extractor.unraveldocs.user.dto.UserData;
import com.extractor.unraveldocs.user.dto.request.ChangePasswordDto;
import com.extractor.unraveldocs.user.dto.request.ForgotPasswordDto;
import com.extractor.unraveldocs.user.dto.request.ProfileUpdateRequestDto;
import com.extractor.unraveldocs.user.dto.request.ResetPasswordDto;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.IPasswordReset;
import com.extractor.unraveldocs.user.interfaces.userimpl.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final GetUserProfileService getUserProfileService;
    private final PasswordResetService passwordResetService;
    private final ChangePasswordService changePasswordService;
    private final ProfileUpdateService profileUpdateService;
    private final DeleteUserService deleteUserService;

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
}
