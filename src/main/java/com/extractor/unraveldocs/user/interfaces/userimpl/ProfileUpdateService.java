package com.extractor.unraveldocs.user.interfaces.userimpl;

import com.extractor.unraveldocs.user.dto.UserData;
import com.extractor.unraveldocs.user.dto.request.ProfileUpdateRequestDto;
import com.extractor.unraveldocs.global.response.UserResponse;

public interface ProfileUpdateService {
    /**
     * Updates the profile of a user.
     *
     * @param request the request containing the updated profile information
     * @param userId  the ID of the user whose profile is to be updated
     * @return the updated user response
     */
    UserResponse<UserData> updateProfile(ProfileUpdateRequestDto request, String userId);
}
