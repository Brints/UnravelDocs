package com.extractor.unraveldocs.user.interfaces.userimpl;

import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.dto.UserData;

public interface GetUserProfileService {
    UserResponse<UserData> getUserProfileByAdmin(String userId);
    UserResponse<UserData> getUserProfileByOwner(String userId);
}
