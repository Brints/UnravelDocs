package com.extractor.unraveldocs.admin.interfaces;

import com.extractor.unraveldocs.global.response.UnravelDocsDataResponse;
import com.extractor.unraveldocs.user.dto.UserData;

public interface GetUserProfileByAdminService {
    UnravelDocsDataResponse<UserData> getUserProfileByAdmin(String userId);
}
