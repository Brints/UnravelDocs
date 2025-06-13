package com.extractor.unraveldocs.admin.interfaces;

import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.dto.UserData;

public interface GetUserProfileByAdminService {
    UserResponse<UserData> getUserProfileByAdmin(String userId);
}
