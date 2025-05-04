package com.extractor.unraveldocs.user.interfaces.userimpl;

import com.extractor.unraveldocs.user.dto.request.ChangePasswordDto;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.IPasswordReset;

public interface ChangePasswordService {
    UserResponse changePassword(IPasswordReset params, ChangePasswordDto request);
}
