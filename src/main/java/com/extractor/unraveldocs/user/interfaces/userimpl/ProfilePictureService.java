package com.extractor.unraveldocs.user.interfaces.userimpl;

import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface ProfilePictureService {
    UserResponse<String> uploadProfilePicture(User user, MultipartFile file);
    UserResponse<Void> deleteProfilePicture(User user);
}
