package com.extractor.unraveldocs.user.interfaces.userimpl;

import com.extractor.unraveldocs.user.dto.response.UserResponse;

public interface UserProfileService {
    UserResponse getUserProfileById(String userId);
    UserResponse getAuthenticatedUserProfile(String email);
}
