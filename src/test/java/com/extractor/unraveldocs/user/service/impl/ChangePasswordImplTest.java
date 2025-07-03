package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.UserEmailTemplateService;
import com.extractor.unraveldocs.security.JwtTokenProvider;
import com.extractor.unraveldocs.security.TokenBlacklistService;
import com.extractor.unraveldocs.user.dto.request.ChangePasswordDto;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.user.impl.ChangePasswordImpl;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private TokenBlacklistService tokenBlacklistService;
    private JwtTokenProvider tokenProvider;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(UserEmailTemplateService.class);
        responseBuilder = mock(ResponseBuilderService.class);
        tokenBlacklistService = mock(TokenBlacklistService.class);
        tokenProvider = mock(JwtTokenProvider.class);
        authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        // Setup security context
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        changePasswordService = new ChangePasswordImpl(
                passwordEncoder,
                responseBuilder,
                tokenBlacklistService,
                tokenProvider,
                emailService,
                userRepository
        );
    }

    @Test
    void changePassword_UserNotFound_ThrowsNotFoundException() {
        // Setup
        ChangePasswordDto request = new ChangePasswordDto("oldPass", "newPass", "newPass");
        when(authentication.getName()).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.empty());

        // Execute & Assert
        assertThrows(NotFoundException.class, () -> changePasswordService.changePassword(request));
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void changePassword_AccountNotVerified_ThrowsForbiddenException() {
        // Setup
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setVerified(false);
        ChangePasswordDto request = new ChangePasswordDto("oldPass", "newPass", "newPass");

        when(authentication.getName()).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));

        // Execute & Assert
        assertThrows(ForbiddenException.class, () -> changePasswordService.changePassword(request));
        verify(userRepository).findByEmail("john.doe@example.com");
    }

    @Test
    void changePassword_WrongOldPassword_ThrowsBadRequestException() {
        // Setup
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setPassword("hashedOldPass");
        user.setVerified(true);
        ChangePasswordDto request = new ChangePasswordDto("wrongOldPass", "newPass", "newPass");

        when(authentication.getName()).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPass", "hashedOldPass")).thenReturn(false);

        // Execute & Assert
        assertThrows(BadRequestException.class, () -> changePasswordService.changePassword(request));
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder).matches("wrongOldPass", "hashedOldPass");
    }

    @Test
    void changePassword_NewPasswordSameAsOld_ThrowsBadRequestException() {
        // Setup
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setPassword("hashedOldPass");
        user.setVerified(true);
        ChangePasswordDto request = new ChangePasswordDto("oldPass", "oldPass", "oldPass");

        when(authentication.getName()).thenReturn("john.doe@example.com");
        when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "hashedOldPass")).thenReturn(true); // Old password matches
        when(passwordEncoder.matches("oldPass", "hashedOldPass")).thenReturn(true); // New password matches old

        // Execute & Assert
        assertThrows(BadRequestException.class, () -> changePasswordService.changePassword(request));
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(passwordEncoder, times(2)).matches(anyString(), anyString());
    }

    @Test
    void changePassword_SuccessfulChange_ReturnsUserResponseAndBlacklistsToken() {
        // Setup
        User user = new User();
        user.setEmail("john.doe@example.com");
        user.setPassword("hashedOldPass");
        user.setVerified(true);
        user.setFirstName("John");
        user.setLastName("Doe");

        ChangePasswordDto request = new ChangePasswordDto("oldPass", "newPass", "newPass");
        String mockToken = "jwt-token-string";
        String jti = "token-jti-123";
        long expirationMs = 3600000L;

        when(authentication.getName()).thenReturn("john.doe@example.com");
        when(authentication.getCredentials()).thenReturn(mockToken);
        when(tokenProvider.getJtiFromToken(mockToken)).thenReturn(jti);
        when(tokenProvider.getAccessExpirationInMs()).thenReturn(expirationMs);

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

        // Execute
        UserResponse<Void> response = changePasswordService.changePassword(request);

        // Assert
        assertNotNull(response);
        assertEquals("Password changed successfully.", response.getMessage());
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("success", response.getStatus());
        assertNull(response.getData());

        // Verify all interactions
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).matches("oldPass", "hashedOldPass");
        verify(passwordEncoder).matches("newPass", "hashedOldPass");
        verify(passwordEncoder).encode("newPass");
        verify(tokenBlacklistService).blacklistToken(jti, expirationMs);
        verify(emailService).sendSuccessfulPasswordChange(
                "john.doe@example.com",
                "John",
                "Doe"
        );
    }
}