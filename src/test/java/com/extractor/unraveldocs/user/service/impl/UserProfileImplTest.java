// src/test/java/com/extractor/unraveldocs/user/service/impl/UserProfileServiceImplTest.java
package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.user.service.ResponseBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        String userId = "123";
        User user = new User();
        UserResponse userResponse = new UserResponse();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(responseBuilder.buildUserResponse(user, "User profile retrieved successfully")).thenReturn(userResponse);

        UserResponse result = userProfile.getUserProfileById(userId);

        assertEquals(userResponse, result);
        verify(userRepository).findById(userId);
        verify(responseBuilder).buildUserResponse(user, "User profile retrieved successfully");
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
        String email = "test@example.com";
        User user = new User();
        UserResponse userResponse = new UserResponse();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(responseBuilder.buildUserResponse(user, "User profile retrieved successfully")).thenReturn(userResponse);

        UserResponse result = userProfile.getAuthenticatedUserProfile(email);

        assertEquals(userResponse, result);
        verify(userRepository).findByEmail(email);
        verify(responseBuilder).buildUserResponse(user, "User profile retrieved successfully");
    }

    @Test
    void getAuthenticatedUserProfile_UserNotFound_ThrowsNotFoundException() {
        String email = "test@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userProfile.getAuthenticatedUserProfile(email));
        verify(userRepository).findByEmail(email);
    }
}