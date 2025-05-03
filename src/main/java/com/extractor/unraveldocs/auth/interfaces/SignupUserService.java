package com.extractor.unraveldocs.auth.interfaces;

import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.dto.response.SignupUserResponse;

public interface SignupUserService {
   SignupUserResponse registerUser(SignUpRequestDto request);
}
