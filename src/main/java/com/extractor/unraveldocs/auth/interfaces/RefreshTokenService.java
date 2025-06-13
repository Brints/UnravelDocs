package com.extractor.unraveldocs.auth.interfaces;

import com.extractor.unraveldocs.auth.dto.RefreshLoginData;
import com.extractor.unraveldocs.auth.dto.request.RefreshTokenRequest;
import com.extractor.unraveldocs.global.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface RefreshTokenService {
    UserResponse<RefreshLoginData> refreshToken(RefreshTokenRequest refreshTokenRequest);
    UserResponse<Void> logout(HttpServletRequest request);
}
