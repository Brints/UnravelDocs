package com.extractor.unraveldocs.auth.dto.response;

import com.extractor.unraveldocs.auth.dto.LoginUserData;
import lombok.Builder;

@Builder
public record UserLoginResponse(
        int status_code,
        String status,
        String message,
        LoginUserData data
) {
}
