package com.extractor.unraveldocs.auth.interfaces;

import com.extractor.unraveldocs.auth.dto.SignupData;
import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.global.response.UserResponse;

public interface SignupUserService {
   UserResponse<SignupData> registerUser(SignUpRequestDto request);
}
