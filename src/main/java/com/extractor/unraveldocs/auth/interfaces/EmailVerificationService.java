package com.extractor.unraveldocs.auth.interfaces;

import com.extractor.unraveldocs.auth.dto.request.ResendEmailVerificationDto;
import com.extractor.unraveldocs.global.response.UnravelDocsDataResponse;

public interface EmailVerificationService {
    UnravelDocsDataResponse<Void> verifyEmail(String email, String token);
    UnravelDocsDataResponse<Void> resendEmailVerification(ResendEmailVerificationDto request);
}
