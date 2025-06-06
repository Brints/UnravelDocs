package com.extractor.unraveldocs.auth.service.impl;

import com.extractor.unraveldocs.auth.dto.LoginData;
import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.auth.interfaces.LoginUserService;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.loginattempts.interfaces.LoginAttemptsService;
import com.extractor.unraveldocs.security.JwtTokenProvider;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginUserImpl implements LoginUserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final ResponseBuilderService responseBuilder;
    private final LoginAttemptsService loginAttemptsService;

    public UserResponse<LoginData> loginUser(LoginRequestDto request) {
        Optional<User> userOpt = userRepository.findByEmail(request.email());

        userOpt.ifPresent(loginAttemptsService::checkIfUserBlocked);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (AuthenticationException e) {
            userOpt.ifPresent(loginAttemptsService::recordFailedLoginAttempt);
            throw new ForbiddenException("Invalid credentials.");
        }

        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        User authenticatedUser = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ForbiddenException("Authenticated user not found in repository."));

        if (!authenticatedUser.isVerified()) {
            throw new BadRequestException("User is not yet verified. Please check your email for verification.");
        }

        loginAttemptsService.resetLoginAttempts(authenticatedUser);

        String jwtToken = jwtTokenProvider.generateToken(authenticatedUser);
        authenticatedUser.setLastLogin(LocalDateTime.now());
        userRepository.save(authenticatedUser);

        LoginData data = LoginData.builder()
                .id(authenticatedUser.getId())
                .firstName(authenticatedUser.getFirstName())
                .lastName(authenticatedUser.getLastName())
                .email(authenticatedUser.getEmail())
                .isVerified(authenticatedUser.isVerified())
                .isActive(authenticatedUser.isActive())
                .role(authenticatedUser.getRole())
                .lastLogin(authenticatedUser.getLastLogin())
                .accessToken(jwtToken)
                .createdAt(authenticatedUser.getCreatedAt())
                .updatedAt(authenticatedUser.getUpdatedAt())
                .build();

        return responseBuilder.buildUserResponse(data, HttpStatus.OK, "User logged in successfully");
    }
}
