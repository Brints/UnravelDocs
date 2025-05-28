package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.dto.UserData;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserProfileImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResponseBuilderService responseBuilder;

    @InjectMocks
    private UserProfileImpl userProfile;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUserProfileById_UserExists_ReturnsUserResponse() {
        String userId = "123"; // Must match getUser().getId()
        User user = getUser();
        UserResponse<UserData> userResponse = new UserResponse<>();
        userResponse.setStatusCode(HttpStatus.OK.value());
        userResponse.setStatus("success");
        userResponse.setMessage("User profile retrieved successfully");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(responseBuilder.buildUserResponse(any(UserData.class), eq(HttpStatus.OK), eq("User profile retrieved successfully")))
                .thenReturn(userResponse);

        UserResponse<UserData> result = userProfile.getUserProfileById(userId);

        assertEquals(userResponse, result);
    }

    @Test
    void getUserProfileById_UserNotFound_ThrowsNotFoundException() {
        String userId = "123";
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userProfile.getUserProfileById(userId));
        verify(userRepository).findById(userId);
    }

    @Test
    void getAuthenticatedUserProfile_UserExists_ReturnsUserResponse() {
        String email = "test@example.com"; // Must match getUser().getEmail()
        User user = getUser();
        UserResponse<UserData> userResponse = new UserResponse<>();
        userResponse.setStatusCode(HttpStatus.OK.value());
        userResponse.setStatus("success");
        userResponse.setMessage("User profile retrieved successfully");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(responseBuilder.buildUserResponse(any(UserData.class), eq(HttpStatus.OK), eq("User profile retrieved successfully")))
                .thenReturn(userResponse);

        UserResponse<UserData> result = userProfile.getAuthenticatedUserProfile(email);

        assertEquals(userResponse, result);
    }

    @Test
    void getAuthenticatedUserProfile_UserNotFound_ThrowsNotFoundException() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userProfile.getAuthenticatedUserProfile(email));
        verify(userRepository).findByEmail(email);
    }

    // Helper methods to create User and UserData objects
    private User getUser() {
        User user = new User();
        user.setId("123");
        user.setProfilePicture("pic.png");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("test@example.com");
        user.setLastLogin(LocalDateTime.now());
        user.setRole(Role.USER);
        user.setVerified(true);
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
}