package com.extractor.unraveldocs.auth.interfaces;

import com.extractor.unraveldocs.auth.dto.request.ResendEmailVerificationDto;
import com.extractor.unraveldocs.global.response.UserResponse;

public interface EmailVerificationService {
    UserResponse<Void> verifyEmail(String email, String token);
    UserResponse<Void> resendEmailVerification(ResendEmailVerificationDto request);
}
