package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.UserEmailTemplateService;
import com.extractor.unraveldocs.user.dto.request.ChangePasswordDto;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.IPasswordReset;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.user.service.ResponseBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangePasswordImplTest {

    private UserRepository userRepository;
    private ChangePasswordImpl changePasswordService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        changePasswordService = new ChangePasswordImpl(
                userRepository,
                mock(PasswordEncoder.class),
                mock(UserEmailTemplateService.class),
                mock(ResponseBuilderService.class)
        );
    }

    @Test
    void changePassword_UserNotFound_ThrowsNotFoundException() {
        ChangePasswordDto request = new ChangePasswordDto("oldPass", "newPass", "newPass");
        IPasswordReset params = mock(IPasswordReset.class);
        when(params.getEmail()).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> changePasswordService.changePassword(
                params,
                request
        ));
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void changePassword_WrongOldPassword_ThrowsBadRequestException() {
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setPassword("hashedOldPass");
        user.setVerified(true); // Assuming the user is verified
        ChangePasswordDto request = new ChangePasswordDto("wrongOldPass", "newPass", "newPass");
        IPasswordReset params = mock(IPasswordReset.class);
        when(params.getEmail()).thenReturn("john.doe@example.com");

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        // Simulate password check failure (you may need to mock a password encoder if used)

        assertThrows(BadRequestException.class, () -> changePasswordService.changePassword(
                params,
                request
        ));
        verify(userRepository).findByEmail("john.doe@example.com");
    }

//    @Test
//    void changePassword_SuccessfulChange_ReturnsUserResponse() {
//        User user = new User();
//        user.setEmail("john.doe@example.com");
//        user.setPassword("oldPass");
//        user.setVerified(true); // Assuming the user is verified
//
//        ChangePasswordDto request = new ChangePasswordDto("oldPass", "newPass", "newPass");
//        IPasswordReset params = mock(IPasswordReset.class);
//        when(params.getEmail()).thenReturn("john.doe@example.com");
//
//        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
//        when(userRepository.save(any(User.class))).thenReturn(user);
//
//        UserResponse response = changePasswordService.changePassword(
//                params,
//                request
//        );
//
//        assertNotNull(response);
//        assertEquals("Password changed successfully", response.getMessage());
//        verify(userRepository).findByEmail("john.doe@example.com");
//        verify(userRepository).save(any(User.class));
//    }
}
