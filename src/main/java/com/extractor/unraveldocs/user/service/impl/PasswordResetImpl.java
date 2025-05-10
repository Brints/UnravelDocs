package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
import com.extractor.unraveldocs.auth.model.UserVerification;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.UserEmailTemplateService;
import com.extractor.unraveldocs.user.dto.request.ForgotPasswordDto;
import com.extractor.unraveldocs.user.dto.request.ResetPasswordDto;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.passwordreset.IPasswordReset;
import com.extractor.unraveldocs.user.interfaces.userimpl.PasswordResetService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.user.service.ResponseBuilderService;
import com.extractor.unraveldocs.utils.generatetoken.GenerateVerificationToken;
import com.extractor.unraveldocs.utils.userlib.DateHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PasswordResetImpl implements PasswordResetService {
    private final UserRepository userRepository;
    private final GenerateVerificationToken generateVerificationToken;
    private final DateHelper dateHelper;
    private final PasswordEncoder passwordEncoder;
    private final UserEmailTemplateService userEmailTemplateService;
    private final ResponseBuilderService responseBuilder;

    public UserResponse forgotPassword(ForgotPasswordDto forgotPasswordDto) {
        String email = forgotPasswordDto.email();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User does not exist."));

        UserVerification userVerification = user.getUserVerification();

        if (!user.isVerified() || !userVerification.isEmailVerified()) {
            throw new BadRequestException("This account is not verified. Please verify your account before resetting the password.");
        }

        if (userVerification.getPasswordResetToken() != null) {
            throw new BadRequestException("A password reset request has already been sent. Please check your email.");
        }

        // Current time
        LocalDateTime currentTime = LocalDateTime.now();

        if (
                userVerification.getPasswordResetTokenExpiry() != null &&
                        userVerification.getPasswordResetTokenExpiry().isAfter(currentTime)
        ){
            String timeLeft = dateHelper.getTimeLeftToExpiry(currentTime, userVerification.getPasswordResetTokenExpiry(), "hours");
            throw new BadRequestException(
                    "A password reset request has already been sent. Token expires in : " + timeLeft);
        }

        String token = generateVerificationToken.generateVerificationToken();
        LocalDateTime expiryTime = dateHelper.setExpiryDate(currentTime,"hour", 1);

        userVerification.setPasswordResetToken(token);
        userVerification.setPasswordResetTokenExpiry(expiryTime);
        userRepository.save(user);

        // TODO: Send email with the token (implementation not shown)
        String expiration = dateHelper.getTimeLeftToExpiry(currentTime, expiryTime, "hours");
        userEmailTemplateService.sendPasswordResetToken(
                email,
                user.getFirstName(),
                user.getLastName(),
                token,
                expiration
        );

        return responseBuilder.buildResponseWithoutData("Password reset link sent to your email.");
    }

    @Override
    public UserResponse resetPassword(IPasswordReset params, ResetPasswordDto request) {
        String email = params.getEmail();
        String token = params.getToken();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User does not exist."));

        UserVerification userVerification = user.getUserVerification();

        if (!user.isVerified() || !userVerification.isEmailVerified()) {
            throw new ForbiddenException("Account not verified. Please verify your account first.");
        }

        if (!userVerification.getPasswordResetToken().equals(token)) {
            throw new BadRequestException("Invalid password reset token.");
        }

        if (userVerification.getPasswordResetTokenExpiry().isBefore(LocalDateTime.now())) {
            userVerification.setStatus(VerifiedStatus.EXPIRED);
            userRepository.save(user);
            throw new BadRequestException("Password reset token has expired.");
        }

        boolean isOldPassword =
                passwordEncoder.matches(request.newPassword(), user.getPassword());
        if (isOldPassword) {
            throw new BadRequestException("New password cannot be the same as the old password.");
        }

        // Update password logic (implementation not shown)
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        user.setPassword(encodedPassword);
        userVerification.setPasswordResetToken(null);
        userVerification.setPasswordResetTokenExpiry(null);
        userVerification.setStatus(VerifiedStatus.VERIFIED);
        userRepository.save(user);

        // TODO: Send email to user with the new password (implementation not shown)
        userEmailTemplateService.sendSuccessfulPasswordReset(
                email,
                user.getFirstName(),
                user.getLastName()
        );

        return responseBuilder.buildResponseWithoutData("Password reset successfully.");
    }
}
