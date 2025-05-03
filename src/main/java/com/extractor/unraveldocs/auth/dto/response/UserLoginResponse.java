package com.extractor.unraveldocs.auth.dto.response;

import com.extractor.unraveldocs.auth.dto.LoginUserData;
import lombok.Builder;

@Builder
public record UserLoginResponse(
        int statusCode,
        String status,
        String message,
        LoginUserData data
) {
}
