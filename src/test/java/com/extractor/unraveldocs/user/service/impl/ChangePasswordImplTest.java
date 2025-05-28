package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.UserEmailTemplateService;
import com.extractor.unraveldocs.user.dto.request.ChangePasswordDto;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.IPasswordReset;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChangePasswordImplTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserEmailTemplateService emailService;
    private ResponseBuilderService responseBuilder;
    private ChangePasswordImpl changePasswordService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(UserEmailTemplateService.class);
        responseBuilder = mock(ResponseBuilderService.class);
        changePasswordService = new ChangePasswordImpl(
                userRepository,
                passwordEncoder,
                emailService,
                responseBuilder
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
    void changePassword_AccountNotVerified_ThrowsForbiddenException() {
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setVerified(false);
        ChangePasswordDto request = new ChangePasswordDto("oldPass", "newPass", "newPass");
        IPasswordReset params = mock(IPasswordReset.class);
        when(params.getEmail()).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        assertThrows(ForbiddenException.class, () -> changePasswordService.changePassword(
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
        user.setVerified(true);
        ChangePasswordDto request = new ChangePasswordDto("wrongOldPass", "newPass", "newPass");
        IPasswordReset params = mock(IPasswordReset.class);
        when(params.getEmail()).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPass", "hashedOldPass")).thenReturn(false);

        assertThrows(BadRequestException.class, () -> changePasswordService.changePassword(
                params,
                request
        ));
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder).matches("wrongOldPass", "hashedOldPass");
    }

    @Test
    void changePassword_NewPasswordSameAsOld_ThrowsBadRequestException() {
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setPassword("hashedOldPass");
        user.setVerified(true);
        ChangePasswordDto request = new ChangePasswordDto("oldPass", "oldPass", "oldPass");
        IPasswordReset params = mock(IPasswordReset.class);
        when(params.getEmail()).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "hashedOldPass")).thenReturn(true); // old password matches
        when(passwordEncoder.matches("oldPass", "hashedOldPass")).thenReturn(true); // new password matches old

        assertThrows(BadRequestException.class, () -> changePasswordService.changePassword(
                params,
                request
        ));
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder, times(2)).matches("oldPass", "hashedOldPass");
    }

    @Test
    void changePassword_SuccessfulChange_ReturnsUserResponse() {
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setPassword("hashedOldPass");
        user.setVerified(true);
        user.setFirstName("John");
        user.setLastName("Doe");

        ChangePasswordDto request = new ChangePasswordDto("oldPass", "newPass", "newPass");
        IPasswordReset params = mock(IPasswordReset.class);
        when(params.getEmail()).thenReturn("john.doe@example.com");

        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "hashedOldPass")).thenReturn(true);
        when(passwordEncoder.matches("newPass", "hashedOldPass")).thenReturn(false);
        when(passwordEncoder.encode("newPass")).thenReturn("hashedNewPass");
        when(userRepository.save(any(User.class))).thenReturn(user);

        var expectedResponse = new UserResponse<>();
        expectedResponse.setStatusCode(HttpStatus.OK.value());
        expectedResponse.setStatus("success");
        expectedResponse.setMessage("Password changed successfully.");
        expectedResponse.setData(null);

        when(responseBuilder.buildUserResponse(null, HttpStatus.OK, "Password changed successfully."))
                .thenReturn(expectedResponse);

        UserResponse<Void> response = changePasswordService.changePassword(
                params,
                request
        );

        assertNotNull(response);
        assertEquals("Password changed successfully.", response.getMessage());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("success", response.getStatus());
        assertNull(response.getData());

        verify(userRepository).findByEmail("john.doe@example.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).matches("oldPass", "hashedOldPass");
        verify(passwordEncoder).matches("newPass", "hashedOldPass");
        verify(passwordEncoder).encode("newPass");
        verify(emailService).sendSuccessfulPasswordChange(
                "john.doe@example.com",
                "John",
                "Doe"
        );
    }
}