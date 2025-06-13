package com.extractor.unraveldocs.auth.service.impl;

import com.extractor.unraveldocs.auth.dto.LoginData;
import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.loginattempts.interfaces.LoginAttemptsService; // Added
import com.extractor.unraveldocs.security.JwtTokenProvider;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException; // Added
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class LoginUserImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ResponseBuilderService responseBuilder;

    @Mock
    private LoginAttemptsService loginAttemptsService;

    @InjectMocks
    private LoginUserImpl loginUserImpl;

    private User user;
    private LoginRequestDto loginRequest;
    private Authentication authentication;
    private UserDetails principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loginRequest = new LoginRequestDto("test@example.com", "password");

        user = new User();
        user.setId("userId");
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setVerified(true);
        user.setActive(true);
        user.setRole(Role.USER);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setCreatedAt(LocalDateTime.now().minusDays(1));
        user.setUpdatedAt(LocalDateTime.now().minusHours(1));


        authentication = mock(Authentication.class);
        principal = new org.springframework.security.core.userdetails.User(
                "test@example.com", "encodedPassword", new ArrayList<>()
        );
    }

    @Test
    void loginUser_successfulLogin_returnsUserLoginResponse() {
        // Arrange
        LoginData loginData = LoginData.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .lastLogin(LocalDateTime.now())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .accessToken("jwtToken")
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();

        UserResponse<LoginData> expectedResponse = new UserResponse<>();
        expectedResponse.setStatusCode(HttpStatus.OK.value());
        expectedResponse.setStatus("success");
        expectedResponse.setMessage("User logged in successfully");
        expectedResponse.setData(loginData);

        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByEmail(principal.getUsername())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("jwtToken");
        when(responseBuilder.buildUserResponse(
                any(LoginData.class),
                eq(HttpStatus.OK),
                eq("User logged in successfully")
        )).thenReturn(expectedResponse);

        // Act
        UserResponse<LoginData> response = loginUserImpl.loginUser(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("success", response.getStatus());
        assertEquals("User logged in successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("jwtToken", response.getData().accessToken());

        verify(loginAttemptsService).checkIfUserBlocked(user);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(2)).findByEmail(eq("test@example.com")); // Adjusted for clarity
        verify(loginAttemptsService).resetLoginAttempts(user);
        verify(jwtTokenProvider).generateAccessToken(user);
        verify(userRepository).save(user);
        verify(responseBuilder).buildUserResponse(any(LoginData.class), eq(HttpStatus.OK), eq("User logged in successfully"));
    }

    @Test
    void loginUser_invalidCredentials_throwsForbiddenExceptionAndRecordsAttempt() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> loginUserImpl.loginUser(loginRequest));
        assertEquals("Invalid credentials.", exception.getMessage());

        verify(loginAttemptsService).checkIfUserBlocked(user);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(loginAttemptsService).recordFailedLoginAttempt(user);
        verify(userRepository, never()).save(any(User.class));
        verify(jwtTokenProvider, never()).generateAccessToken(any(User.class));
        verify(responseBuilder, never()).buildUserResponse(any(), any(), anyString());
    }

    @Test
    void loginUser_userNotVerified_throwsBadRequestException() {
        // Arrange
        user.setVerified(false);
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByEmail(principal.getUsername())).thenReturn(Optional.of(user));


        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> loginUserImpl.loginUser(loginRequest));
        assertEquals("User is not yet verified. Please check your email for verification.", exception.getMessage());

        verify(loginAttemptsService).checkIfUserBlocked(user);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(loginAttemptsService, never()).recordFailedLoginAttempt(any(User.class));
        verify(loginAttemptsService, never()).resetLoginAttempts(any(User.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void loginUser_authenticatedUserNotFoundInRepository_throwsForbiddenException() {
        // Arrange
        String userEmail = loginRequest.email();

        when(userRepository.findByEmail(eq(userEmail)))
                .thenReturn(Optional.of(user))     // First call for userOpt
                .thenReturn(Optional.empty());    // Second call for authenticatedUser

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> loginUserImpl.loginUser(loginRequest));
        assertEquals("Authenticated user not found in repository.", exception.getMessage());

        verify(loginAttemptsService).checkIfUserBlocked(user); // or any(User.class)
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(2)).findByEmail(eq(userEmail));
        verify(loginAttemptsService, never()).recordFailedLoginAttempt(any(User.class));
        verify(loginAttemptsService, never()).resetLoginAttempts(any(User.class));
    }

    @Test
    void loginUser_userNotFoundByRequestEmail_throwsForbiddenException() {
        // Arrange
        when(userRepository.findByEmail(loginRequest.email())).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));


        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> loginUserImpl.loginUser(loginRequest));
        assertEquals("Invalid credentials.", exception.getMessage());

        verify(userRepository).findByEmail(loginRequest.email());
        verify(loginAttemptsService, never()).checkIfUserBlocked(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(loginAttemptsService, never()).recordFailedLoginAttempt(any(User.class));
        verify(responseBuilder, never()).buildUserResponse(any(), any(), anyString());
    }

    @Test
    void loginUser_userIsBlocked_throwsForbiddenException() {
        // Arrange
        String blockedUserEmail = "blocked@example.com";
        LoginRequestDto blockedLoginRequest = new LoginRequestDto(blockedUserEmail, "password");
        User blockedUser = new User();
        blockedUser.setEmail(blockedUserEmail);
        blockedUser.setId("blockedUserId");

        when(userRepository.findByEmail(blockedLoginRequest.email())).thenReturn(Optional.of(blockedUser));
        String expectedMessage = """
            Your account is temporarily locked due to multiple failed login attempts.
            Please try again after 1 day.
            """;
        doThrow(new ForbiddenException(expectedMessage))
                .when(loginAttemptsService).checkIfUserBlocked(blockedUser);

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> loginUserImpl.loginUser(blockedLoginRequest));
        assertEquals(expectedMessage, exception.getMessage());

        verify(userRepository).findByEmail(blockedLoginRequest.email());
        verify(loginAttemptsService).checkIfUserBlocked(blockedUser);
        verify(authenticationManager, never()).authenticate(any());
        verify(loginAttemptsService, never()).recordFailedLoginAttempt(any());
        verify(loginAttemptsService, never()).resetLoginAttempts(any());
        verify(responseBuilder, never()).buildUserResponse(any(), any(), anyString());
    }
}