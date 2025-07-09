package com.extractor.unraveldocs.user.impl;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.UserEmailTemplateService;
import com.extractor.unraveldocs.security.JwtTokenProvider;
import com.extractor.unraveldocs.security.TokenBlacklistService;
import com.extractor.unraveldocs.user.dto.request.ChangePasswordDto;
import com.extractor.unraveldocs.global.response.UnravelDocsDataResponse;
import com.extractor.unraveldocs.user.interfaces.userimpl.ChangePasswordService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangePasswordImpl implements ChangePasswordService {

    private final PasswordEncoder passwordEncoder;
    private final ResponseBuilderService responseBuilder;
    private final TokenBlacklistService tokenBlacklist;
    private final JwtTokenProvider tokenProvider;
    private final UserEmailTemplateService userEmailTemplateService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UnravelDocsDataResponse<Void> changePassword(ChangePasswordDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User does not exist."));

        if (!user.isVerified()) {
            throw new ForbiddenException("Account not verified. Please verify your account first.");
        }

        boolean oldPassword = passwordEncoder.matches(request.oldPassword(), user.getPassword());
        if (!oldPassword) {
            throw new BadRequestException("Old password is incorrect.");
        }

        boolean isOldPassword =
                passwordEncoder.matches(request.newPassword(), user.getPassword());
        if (isOldPassword) {
            throw new BadRequestException("New password cannot be the same as the old password.");
        }

        // Update password logic (implementation not shown)
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // Invalidate all tokens for the user
        String jti;
        if (authentication.getCredentials() instanceof String token) {
            jti = tokenProvider.getJtiFromToken(token);
            if (jti != null) {
                tokenBlacklist.blacklistToken(jti, tokenProvider.getAccessExpirationInMs());
            }
        }

        userEmailTemplateService.sendSuccessfulPasswordChange(email, user.getFirstName(), user.getLastName());

        log.info("Password changed successfully for user: {}", email);

        return responseBuilder
                .buildUserResponse(null, HttpStatus.OK, "Password changed successfully.");
    }
}
