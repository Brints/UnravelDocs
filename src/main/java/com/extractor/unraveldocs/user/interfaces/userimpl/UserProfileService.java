package com.extractor.unraveldocs.user.interfaces.userimpl;

import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.dto.UserData;

public interface UserProfileService {
    UserResponse<UserData> getUserProfileById(String userId);
    UserResponse<UserData> getAuthenticatedUserProfile(String email);
}
