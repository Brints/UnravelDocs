package com.extractor.unraveldocs.auth.impl;

import com.extractor.unraveldocs.auth.dto.SignupData;
import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
import com.extractor.unraveldocs.auth.interfaces.SignupUserService;
import com.extractor.unraveldocs.auth.model.UserVerification;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ConflictException;
import com.extractor.unraveldocs.global.response.ResponseBuilderService;
import com.extractor.unraveldocs.global.response.UserResponse;
import com.extractor.unraveldocs.loginattempts.model.LoginAttempts;
import com.extractor.unraveldocs.messaging.emailtemplates.AuthEmailTemplateService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.utils.generatetoken.GenerateVerificationToken;
import com.extractor.unraveldocs.utils.userlib.DateHelper;
import com.extractor.unraveldocs.utils.userlib.UserLibrary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignupUserImpl implements SignupUserService {
    private final AuthEmailTemplateService templatesService;
    private final DateHelper dateHelper;
    private final GenerateVerificationToken verificationToken;
    private final PasswordEncoder passwordEncoder;
    private final ResponseBuilderService responseBuilder;
    private final UserLibrary userLibrary;
    private final UserRepository userRepository;

    @Transactional
    public UserResponse<SignupData> registerUser(SignUpRequestDto request) {
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

        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime emailVerificationTokenExpiry = dateHelper.setExpiryDate(now,"hour", 3);

        boolean noSuperAdmin = userRepository.superAdminExists();
        Role role = noSuperAdmin ? Role.SUPER_ADMIN : Role.USER;

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPassword(encryptedPassword);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setProfilePicture(null);
        user.setActive(false);
        user.setVerified(false);
        user.setRole(role);
        user.setLastLogin(null);

        UserVerification userVerification = new UserVerification();
        userVerification.setEmailVerificationToken(emailVerificationToken);
        userVerification.setStatus(VerifiedStatus.PENDING);
        userVerification.setEmailVerificationTokenExpiry(emailVerificationTokenExpiry);
        userVerification.setEmailVerified(false);
        userVerification.setPasswordResetToken(null);
        userVerification.setPasswordResetTokenExpiry(null);
        userVerification.setUser(user);

        user.setUserVerification(userVerification);

        LoginAttempts loginAttempts = new LoginAttempts();
        loginAttempts.setUser(user);

        user.setLoginAttempts(loginAttempts);

        userRepository.save(user);

        // TODO: Send email with the verification token (implementation not shown)
        templatesService.sendVerificationEmail(user.getEmail(),
                firstName,
                lastName,
                emailVerificationToken,
                dateHelper.getTimeLeftToExpiry(now, emailVerificationTokenExpiry, "hour"));

        SignupData signupData = SignupData.builder()
                .id(user.getId())
                .profilePicture(user.getProfilePicture())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .lastLogin(user.getLastLogin())
                .isActive(user.isActive())
                .isVerified(user.isVerified())
                .build();

        return responseBuilder.buildUserResponse(signupData, HttpStatus.CREATED, "User registered successfully");
    }
}
