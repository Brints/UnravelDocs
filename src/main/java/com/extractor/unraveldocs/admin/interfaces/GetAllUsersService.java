package com.extractor.unraveldocs.admin.interfaces;

import com.extractor.unraveldocs.admin.dto.request.UserFilterDto;
import com.extractor.unraveldocs.admin.dto.response.UserListData;
import com.extractor.unraveldocs.global.response.UserResponse;

public interface GetAllUsersService {
    UserResponse<UserListData> getAllUsers(UserFilterDto request);
}
