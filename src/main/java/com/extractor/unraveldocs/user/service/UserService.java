package com.extractor.unraveldocs.user.service;

import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.user.dto.UserData;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse userProfile(String userId, @AuthenticationPrincipal UserDetails authenticatedUser) {
        if (userId == null || userId.isEmpty()) {
            throw new BadRequestException("User ID cannot be null or empty");
        }

        User loggedInUser = userRepository.findByEmail(authenticatedUser.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (!loggedInUser.getId().equals(user.getId()) && loggedInUser.getRole() != Role.ADMIN) {
            throw new ForbiddenException("You do not have permission to view this profile");
        }

        return buildUserResponse(user);
    }

    private UserResponse buildUserResponse(User user) {
        UserData userData = new UserData();
        userData.setId(user.getId());
        userData.setProfilePicture(user.getProfilePicture());
        userData.setFirstName(user.getFirstName());
        userData.setLastName(user.getLastName());
        userData.setEmail(user.getEmail());
        userData.setRole(user.getRole());
        userData.setLastLogin(user.getLastLogin());
        userData.setVerified(user.isVerified());
        userData.setCreatedAt(user.getCreatedAt());
        userData.setUpdatedAt(user.getUpdatedAt());

        UserResponse userResponse = new UserResponse();
        userResponse.setStatus_code(200);
        userResponse.setStatus("success");
        userResponse.setMessage("User profile retrieved successfully");
        userResponse.setData(userData);

        return userResponse;
    }
}
