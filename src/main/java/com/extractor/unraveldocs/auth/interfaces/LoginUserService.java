package com.extractor.unraveldocs.auth.interfaces;

import com.extractor.unraveldocs.auth.dto.LoginData;
import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.global.response.UserResponse;

public interface LoginUserService {
    UserResponse<LoginData> loginUser(LoginRequestDto request);
}
