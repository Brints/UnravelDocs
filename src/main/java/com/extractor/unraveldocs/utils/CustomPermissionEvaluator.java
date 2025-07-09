package com.extractor.unraveldocs.utils;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        String currentUserId = authentication.getName();
        String requestedUserId = (String) targetId;

        if ("VIEW_PROFILE".equals(permission)) {
            return currentUserId.equals(requestedUserId);
        }

        return false;
    }
}
