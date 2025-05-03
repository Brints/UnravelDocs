package com.extractor.unraveldocs.user.interfaces.userimpl;

import com.extractor.unraveldocs.user.dto.request.ForgotPasswordDto;
import com.extractor.unraveldocs.user.dto.request.ResetPasswordDto;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.IPasswordReset;

public interface PasswordResetService {
    UserResponse resetPassword(IPasswordReset params, ResetPasswordDto request);
    UserResponse forgotPassword(ForgotPasswordDto forgotPasswordDto);
}
