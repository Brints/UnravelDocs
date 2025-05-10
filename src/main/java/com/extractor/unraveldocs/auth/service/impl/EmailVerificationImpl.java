package com.extractor.unraveldocs.auth.service.impl;

import com.extractor.unraveldocs.auth.dto.request.ResendEmailVerificationDto;
import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
import com.extractor.unraveldocs.auth.interfaces.EmailVerificationService;
import com.extractor.unraveldocs.auth.model.UserVerification;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.AuthEmailTemplateService;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.user.service.ResponseBuilderService;
import com.extractor.unraveldocs.utils.generatetoken.GenerateVerificationToken;
import com.extractor.unraveldocs.utils.userlib.DateHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationImpl implements EmailVerificationService {
    private final UserRepository userRepository;
    private final GenerateVerificationToken verificationToken;
    private final DateHelper dateHelper;
    private final AuthEmailTemplateService templatesService;
    private final ResponseBuilderService responseBuilder;

    public UserResponse resendEmailVerification(ResendEmailVerificationDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User does not exist."));

        if (user.isVerified()) {
            throw new BadRequestException("User is already verified. Please login.");
        }

        // Check if the user already has an active verification token
        UserVerification userVerification = user.getUserVerification();
        LocalDateTime now = LocalDateTime.now();
        if (userVerification.getEmailVerificationToken() != null) {
            String timeLeft = dateHelper.getTimeLeftToExpiry(now, userVerification.getEmailVerificationTokenExpiry(),
                    "hour");
            throw new BadRequestException(
                    "A verification email has already been sent. Token expires in: " + timeLeft);
        }

        String emailVerificationToken = verificationToken.generateVerificationToken();
        LocalDateTime emailVerificationTokenExpiry = dateHelper.setExpiryDate(now, "hour", 3);

        userVerification.setEmailVerificationToken(emailVerificationToken);
        userVerification.setEmailVerificationTokenExpiry(emailVerificationTokenExpiry);
        userVerification.setStatus(VerifiedStatus.PENDING);
        userVerification.setEmailVerified(false);

        userRepository.save(user);

        UserResponse response = new UserResponse();
        response.setStatusCode(HttpStatus.OK.value());
        response.setStatus("success");
        response.setMessage("Verification email resent successfully");
        response.setData(null); // No data to return

        // TODO: Send email with the verification token (implementation not shown)
        templatesService.sendVerificationEmail(user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                emailVerificationToken,
                dateHelper.getTimeLeftToExpiry(now, emailVerificationTokenExpiry, "hour"));

        return response;
    }

    @Transactional
    public UserResponse verifyEmail(String email, String token) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User does not exist."));

        if (user.isVerified()) {
            throw new BadRequestException("User is already verified. Please login.");
        }

        UserVerification userVerification = user.getUserVerification();
        if (!userVerification.getEmailVerificationToken().equals(token)) {
            throw new BadRequestException("Invalid email verification token.");
        }

        if (userVerification.getEmailVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            userVerification.setStatus(VerifiedStatus.EXPIRED);
            userRepository.save(user);
            throw new BadRequestException("Email verification token has expired.");
        }

        userVerification.setEmailVerificationToken(null);
        userVerification.setEmailVerified(true);
        userVerification.setEmailVerificationTokenExpiry(null);
        userVerification.setStatus(VerifiedStatus.VERIFIED);

        user.setVerified(userVerification.getStatus().equals(VerifiedStatus.VERIFIED));
        user.setActive(true);

        userRepository.save(user);

        return responseBuilder.buildResponseWithoutData("Email verified successfully");
    }
}
