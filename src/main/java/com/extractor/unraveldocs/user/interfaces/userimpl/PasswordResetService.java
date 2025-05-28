package com.extractor.unraveldocs.user.interfaces.userimpl;

import com.extractor.unraveldocs.user.dto.request.ForgotPasswordDto;
import com.extractor.unraveldocs.user.dto.request.ResetPasswordDto;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.IPasswordReset;

public interface PasswordResetService {
    UserResponse<Void> resetPassword(IPasswordReset params, ResetPasswordDto request);
    UserResponse<Void> forgotPassword(ForgotPasswordDto forgotPasswordDto);
}
