package com.extractor.unraveldocs.admin.interfaces;

import com.extractor.unraveldocs.admin.dto.AdminData;
import com.extractor.unraveldocs.admin.dto.request.ChangeRoleDto;
import com.extractor.unraveldocs.global.response.UserResponse;
import org.springframework.security.core.Authentication;

public interface ChangeUserRoleService {
    UserResponse<AdminData> changeUserRole(ChangeRoleDto request, Authentication authentication);
}
