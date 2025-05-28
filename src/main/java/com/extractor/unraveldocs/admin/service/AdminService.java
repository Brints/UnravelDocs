package com.extractor.unraveldocs.admin.service;

import com.extractor.unraveldocs.admin.dto.AdminData;
import com.extractor.unraveldocs.admin.dto.request.ChangeRoleDto;
import com.extractor.unraveldocs.admin.interfaces.ChangeUserRoleService;
import com.extractor.unraveldocs.global.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final ChangeUserRoleService changeRoleService;

    public UserResponse<AdminData> changeUserRole(ChangeRoleDto request, Authentication authentication) {
        return changeRoleService.changeUserRole(request, authentication);
    }
}
