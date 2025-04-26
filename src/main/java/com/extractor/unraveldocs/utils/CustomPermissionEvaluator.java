package com.extractor.unraveldocs.utils;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        // This method is not used in your case since you're passing userId directly
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // targetId is the userId from #userId
        // permission is 'VIEW_PROFILE'
        // authentication contains the current user's details

        String currentUserId = authentication.getName(); // Assuming userId is the username
        String requestedUserId = (String) targetId;

        // Example logic: Allow users to view their own profile
        if ("VIEW_PROFILE".equals(permission)) {
            return currentUserId.equals(requestedUserId); // User can view their own profile
        }

        return false; // Deny by default
    }
}
