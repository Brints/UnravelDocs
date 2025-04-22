package com.extractor.unraveldocs.auth.dto.response;

import com.extractor.unraveldocs.auth.dto.SignupUserData;
import lombok.Builder;

@Builder
public record SignupUserResponse(
        int status_code,
        String status,
        String message,
        SignupUserData data
) {
}
