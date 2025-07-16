package com.extractor.unraveldocs.auth.impl;

import com.extractor.unraveldocs.auth.dto.LoginData;
import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.auth.interfaces.LoginUserService;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.TokenProcessingException;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import com.extractor.unraveldocs.global.response.UnravelDocsDataResponse;
import com.extractor.unraveldocs.loginattempts.interfaces.LoginAttemptsService;
import com.extractor.unraveldocs.security.JwtTokenProvider;
import com.extractor.unraveldocs.security.RefreshTokenService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginUserImpl implements LoginUserService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final LoginAttemptsService loginAttemptsService;
    private final ResponseBuilderService responseBuilder;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;

    @Override
    public UnravelDocsDataResponse<LoginData> loginUser(LoginRequestDto request) {
        Optional<User> userOpt = userRepository.findByEmail(request.email());

        userOpt.ifPresent(loginAttemptsService::checkIfUserBlocked);

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            userOpt.ifPresent(loginAttemptsService::recordFailedLoginAttempt);
            throw new BadRequestException("Invalid email or password");
        } catch (DisabledException e) {
            throw new BadRequestException("User account is disabled. Please verify your email or contact support.");
        } catch (LockedException e) {
            throw new ForbiddenException("User account is locked. Please contact support or try again later.");
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user {}: {}", request.email(), e.getMessage());
            userOpt.ifPresent(loginAttemptsService::recordFailedLoginAttempt);
            throw new BadRequestException("Authentication failed. Please check your credentials.");
        }

        User authenticatedUser = (User) authentication.getPrincipal();

        loginAttemptsService.resetLoginAttempts(authenticatedUser);

        String accessToken = jwtTokenProvider.generateAccessToken(authenticatedUser);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authenticatedUser);

        String refreshTokenJti = jwtTokenProvider.getJtiFromToken(refreshToken);
        if (refreshTokenJti != null) {
            refreshTokenService.storeRefreshToken(refreshTokenJti, authenticatedUser.getId());
        } else {
            log.error("Could not generate JTI for refresh token for user {}", authenticatedUser.getEmail());
            throw new TokenProcessingException("Error processing refresh token.");
        }

        if (authenticatedUser.getDeletedAt() != null) {
            authenticatedUser.setDeletedAt(null);
            authenticatedUser.setActive(true);
            if (authenticatedUser.getUserVerification() != null) {
                authenticatedUser.getUserVerification().setDeletedAt(null);
            }
        }

        authenticatedUser.setLastLogin(OffsetDateTime.now());
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
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .createdAt(authenticatedUser.getCreatedAt())
                .updatedAt(authenticatedUser.getUpdatedAt())
                .build();

        return responseBuilder.buildUserResponse(data, HttpStatus.OK, "User logged in successfully");
    }
}
