package com.extractor.unraveldocs.auth.service.impl;

import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.dto.response.SignupUserResponse;
import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
import com.extractor.unraveldocs.auth.interfaces.SignupUserService;
import com.extractor.unraveldocs.auth.model.UserVerification;
import com.extractor.unraveldocs.auth.service.AuthResponseBuilderService;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ConflictException;
import com.extractor.unraveldocs.messaging.emailtemplates.AuthEmailTemplateService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.utils.generatetoken.GenerateVerificationToken;
import com.extractor.unraveldocs.utils.imageupload.aws.AwsS3Service;
import com.extractor.unraveldocs.utils.userlib.DateHelper;
import com.extractor.unraveldocs.utils.userlib.UserLibrary;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupUserImpl implements SignupUserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLibrary userLibrary;
    private final GenerateVerificationToken verificationToken;
    private final DateHelper dateHelper;
    private final AwsS3Service awsS3Service;
    private final AuthEmailTemplateService templatesService;
    private final AuthResponseBuilderService responseBuilder;

    @Transactional
    public SignupUserResponse registerUser(SignUpRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }

        if (request.password().equalsIgnoreCase(request.email())) {
            throw new BadRequestException("Password cannot be same as email.");
        }

        String firstName = userLibrary.capitalizeFirstLetterOfName(request.firstName());
        String lastName = userLibrary.capitalizeFirstLetterOfName(request.lastName());

        String encryptedPassword = passwordEncoder.encode(request.password());
        String emailVerificationToken = verificationToken.generateVerificationToken();
        LocalDateTime emailVerificationTokenExpiry = dateHelper.setExpiryDate("hour", 3);

        boolean userCount = userRepository.isFirstUserWithLock();

        UserVerification userVerification = new UserVerification();
        userVerification.setEmailVerificationToken(emailVerificationToken);
        userVerification.setStatus(VerifiedStatus.PENDING);
        userVerification.setEmailVerificationTokenExpiry(emailVerificationTokenExpiry);
        userVerification.setEmailVerified(false);
        userVerification.setPasswordResetToken(null);
        userVerification.setPasswordResetTokenExpiry(null);

        String profilePictureUrl = null;

        if (request.profilePicture() != null && !request.profilePicture().isEmpty()) {
            try {
                String fileName = awsS3Service.generateFileName(request.profilePicture().getOriginalFilename());
                profilePictureUrl = awsS3Service.uploadFile(request.profilePicture(), fileName);
            } catch (Exception e) {
                log.error("Error uploading profile picture: {}", e.getMessage());
                throw new BadRequestException("Failed to upload profile picture");
            }
        }

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPassword(encryptedPassword);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setProfilePicture(profilePictureUrl);
        user.setActive(false);
        user.setVerified(false);
        user.setRole(userCount ? Role.ADMIN : Role.USER);
        user.setLastLogin(null);
        user.setUserVerification(userVerification);

        userRepository.save(user);

        // TODO: Send email with the verification token (implementation not shown)
        templatesService.sendVerificationEmail(user.getEmail(),
                firstName,
                lastName,
                emailVerificationToken,
                dateHelper.getTimeLeftToExpiry(emailVerificationTokenExpiry, "hour"));

        return responseBuilder.buildUserSignupResponse(user);
    }
}
