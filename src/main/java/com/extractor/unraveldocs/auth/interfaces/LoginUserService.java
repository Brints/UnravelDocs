package com.extractor.unraveldocs.auth.interfaces;

import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.auth.dto.response.UserLoginResponse;

public interface LoginUserService {
    UserLoginResponse loginUser(LoginRequestDto request);
}
