package com.extractor.unraveldocs.auth.interfaces;

import com.extractor.unraveldocs.auth.dto.request.ResendEmailVerificationDto;
import com.extractor.unraveldocs.auth.dto.response.VerifyEmailResponse;
import com.extractor.unraveldocs.user.dto.response.UserResponse;

public interface EmailVerificationService {
    VerifyEmailResponse verifyEmail(String email, String token);
    UserResponse resendEmailVerification(ResendEmailVerificationDto request);
}
