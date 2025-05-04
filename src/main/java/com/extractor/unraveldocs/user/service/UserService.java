package com.extractor.unraveldocs.user.service;

import com.extractor.unraveldocs.user.dto.request.ChangePasswordDto;
import com.extractor.unraveldocs.user.dto.request.ForgotPasswordDto;
import com.extractor.unraveldocs.user.dto.request.ResetPasswordDto;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.IPasswordReset;
import com.extractor.unraveldocs.user.interfaces.userimpl.ChangePasswordService;
import com.extractor.unraveldocs.user.interfaces.userimpl.PasswordResetService;
import com.extractor.unraveldocs.user.interfaces.userimpl.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserProfileService userProfileService;
    private final PasswordResetService passwordResetService;
    private final ChangePasswordService changePasswordService;

    public UserResponse getAuthenticatedUserProfile(String email) {
        return userProfileService.getAuthenticatedUserProfile(email);
    }

    public UserResponse getUserProfileById(String userId) {
        return userProfileService.getUserProfileById(userId);
    }

    public UserResponse forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        return passwordResetService.forgotPassword(forgotPasswordDto);
    }

    public UserResponse resetPassword(IPasswordReset params, ResetPasswordDto request) {
        return passwordResetService.resetPassword(params, request);
    }

    public UserResponse changePassword(IPasswordReset params, ChangePasswordDto request) {
        return changePasswordService.changePassword(params, request);
    }
}
