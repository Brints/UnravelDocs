package com.extractor.unraveldocs.auth.interfaces;

import com.extractor.unraveldocs.auth.dto.request.GeneratePasswordDto;
import com.extractor.unraveldocs.user.dto.response.GeneratePasswordResponse;

public interface GeneratePasswordService {
    GeneratePasswordResponse generatePassword(GeneratePasswordDto passwordDto);
}
