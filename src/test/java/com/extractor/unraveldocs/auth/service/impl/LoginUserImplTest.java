package com.extractor.unraveldocs.auth.service.impl;

import com.extractor.unraveldocs.auth.dto.LoginUserData;
import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.auth.dto.response.UserLoginResponse;
import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.auth.service.AuthResponseBuilderService;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginUserImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private AuthResponseBuilderService responseBuilder;

    @InjectMocks
    private LoginUserImpl loginUserImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loginUser_successfulLogin_returnsUserLoginResponse() {
        LoginRequestDto request = new LoginRequestDto("test@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                "test@example.com", "encodedPassword", new java.util.ArrayList<>()
        );
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setVerified(true);

        LoginUserData loginUserData = LoginUserData.builder()
                .id("userId")
                .firstName("John")
                .lastName("Doe")
                .email("test@example.com")
                .role(Role.USER)
                .lastLogin(LocalDateTime.now())
                .isActive(true)
                .isVerified(true)
                .accessToken("jwtToken")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        UserLoginResponse expectedResponse = new UserLoginResponse(
                HttpStatus.OK.value(), "success", "Login successful", loginUserData
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(user)).thenReturn("jwtToken");
        when(responseBuilder.buildUserLoginResponse(user, "jwtToken")).thenReturn(expectedResponse);

        UserLoginResponse response = loginUserImpl.loginUser(request);

        assertNotNull(response);
        verify(userRepository).save(user);
    }

    @Test
    void loginUser_invalidCredentials_throwsForbiddenException() {
        LoginRequestDto request = new LoginRequestDto("test@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                "test@example.com", "encodedPassword", new java.util.ArrayList<>()
        );
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setVerified(true);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> loginUserImpl.loginUser(request));
    }

    @Test
    void loginUser_userNotVerified_throwsBadRequestException() {
        LoginRequestDto request = new LoginRequestDto("test@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                "test@example.com", "encodedPassword", new java.util.ArrayList<>()
        );
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setVerified(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);

        assertThrows(BadRequestException.class, () -> loginUserImpl.loginUser(request));
    }

    @Test
    void loginUser_userNotFound_throwsForbiddenException() {
        LoginRequestDto request = new LoginRequestDto("test@example.com", "password");
        Authentication authentication = mock(Authentication.class);
        UserDetails principal = new org.springframework.security.core.userdetails.User(
                "test@example.com", "encodedPassword", new java.util.ArrayList<>()
        );

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(principal);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> loginUserImpl.loginUser(request));
    }
}