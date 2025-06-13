package com.extractor.unraveldocs.auth.service;

import com.extractor.unraveldocs.auth.dto.LoginData;
import com.extractor.unraveldocs.auth.dto.RefreshLoginData;
import com.extractor.unraveldocs.auth.dto.SignupData;
import com.extractor.unraveldocs.auth.dto.request.*;
import com.extractor.unraveldocs.auth.interfaces.*;
import com.extractor.unraveldocs.user.dto.response.GeneratePasswordResponse;
import com.extractor.unraveldocs.global.response.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final SignupUserService signupUserService;
    private final LoginUserService loginUserService;
    private final EmailVerificationService emailVerificationService;
    private final GeneratePasswordService generatePasswordService;
    private final RefreshTokenService refreshTokenService;

    public UserResponse<SignupData> registerUser(SignUpRequestDto request) {
        return signupUserService.registerUser(request);
    }

    public UserResponse<LoginData> loginUser(LoginRequestDto request) {
        return loginUserService.loginUser(request);
    }

    public UserResponse<Void> verifyEmail(String email, String token) {
        return emailVerificationService.verifyEmail(email, token);
    }

    public UserResponse<Void> resendEmailVerification(ResendEmailVerificationDto request) {
        return emailVerificationService.resendEmailVerification(request);
    }

    public GeneratePasswordResponse generatePassword(GeneratePasswordDto passwordDto) {
        return generatePasswordService.generatePassword(passwordDto);
    }

    public UserResponse<RefreshLoginData> refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.refreshToken(request);
    }

    public UserResponse<Void> logout(HttpServletRequest request) {
        return refreshTokenService.logout(request);
    }
}