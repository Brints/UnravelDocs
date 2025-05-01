package com.extractor.unraveldocs.auth.service;

import com.extractor.unraveldocs.auth.dto.LoginUserData;
import com.extractor.unraveldocs.auth.dto.SignupUserData;
import com.extractor.unraveldocs.auth.dto.request.GeneratePasswordDto;
import com.extractor.unraveldocs.auth.dto.request.LoginRequestDto;
import com.extractor.unraveldocs.auth.dto.request.ResendEmailVerificationDto;
import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.dto.response.SignupUserResponse;
import com.extractor.unraveldocs.auth.dto.response.UserLoginResponse;
import com.extractor.unraveldocs.auth.dto.response.VerifyEmailResponse;
import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
import com.extractor.unraveldocs.auth.model.UserVerification;
import com.extractor.unraveldocs.messaging.emailtemplates.AuthEmailTemplateService;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ConflictException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.user.dto.GeneratedPassword;
import com.extractor.unraveldocs.user.dto.response.GeneratePasswordResponse;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.utils.imageupload.aws.AwsS3Service;
import com.extractor.unraveldocs.utils.generatetoken.GenerateVerificationToken;
import com.extractor.unraveldocs.utils.jwt.JwtTokenProvider;
import com.extractor.unraveldocs.utils.userlib.DateHelper;
import com.extractor.unraveldocs.utils.userlib.UserLibrary;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLibrary userLibrary;
    private final GenerateVerificationToken verificationToken;
    private final DateHelper dateHelper;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final AwsS3Service awsS3Service;
    private final AuthEmailTemplateService templatesService;

    @Transactional
    public SignupUserResponse registerUser(SignUpRequestDto request, MultipartFile profilePicture) {
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

        if (profilePicture != null && !profilePicture.isEmpty()) {
            try {
                String fileName = "profile_pictures/" + UUID.randomUUID() + "-" + profilePicture.getOriginalFilename();
                profilePictureUrl = awsS3Service.uploadFile(profilePicture, fileName);
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

        return buildUserSignupResponse(user);
    }

    public GeneratePasswordResponse generatePassword(GeneratePasswordDto passwordDto) {
        int convertedLength = Integer.parseInt(passwordDto.passwordLength());
        if (convertedLength < 8) {
            throw new BadRequestException("Length should be greater than 8");
        }

        // Check if user provides a string of excluded characters
        String excludedChars = passwordDto.excludedChars();
        String generatedPassword;
        if (excludedChars != null && !excludedChars.isEmpty()) {
            char[] excludedCharsArray = excludedChars.toCharArray();
            generatedPassword =
                    userLibrary.generateStrongPassword(convertedLength, excludedCharsArray);
        } else {
            generatedPassword = userLibrary.generateStrongPassword(convertedLength);
        }

       GeneratePasswordResponse response = new GeneratePasswordResponse();
        response.setStatusCode(HttpStatus.OK.value());
        response.setStatus("success");
        response.setMessage("Password successfully generated.");
        response.setData(GeneratedPassword.builder()
                .generatedPassword(generatedPassword)
                .build());

        return response;
    }

    public UserLoginResponse loginUser(LoginRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        User user = userRepository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ForbiddenException("Invalid credentials."));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ForbiddenException("Invalid credentials.");
        }

        if (!user.isVerified()) {
            throw new BadRequestException("User is not yet verified. Please check your email for verification.");
        }

        String jwtToken = jwtTokenProvider.generateToken(user);
        user.setLastLogin(LocalDateTime.now());

        userRepository.save(user);

        return buildUserLoginResponse(user, jwtToken);
    }

    @Transactional
    public VerifyEmailResponse verifyEmail(String email, String token) {
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

        return VerifyEmailResponse.builder()
                .status_code(HttpStatus.OK.value())
                .status("success")
                .message("Email verified successfully")
                .build();
    }

    public UserResponse resendEmailVerification(ResendEmailVerificationDto request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User does not exist."));

        if (user.isVerified()) {
            throw new BadRequestException("User is already verified. Please login.");
        }

        // Check if the user already has an active verification token
        UserVerification userVerification = user.getUserVerification();
        if (userVerification.getEmailVerificationToken() != null) {
            String timeLeft = dateHelper.getTimeLeftToExpiry(userVerification.getEmailVerificationTokenExpiry(),
                    "hour");
            throw new BadRequestException(
                    "A verification email has already been sent. Token expires in: " + timeLeft);
        }

        String emailVerificationToken = verificationToken.generateVerificationToken();
        LocalDateTime emailVerificationTokenExpiry = dateHelper.setExpiryDate("hour", 3);

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
                dateHelper.getTimeLeftToExpiry(emailVerificationTokenExpiry, "hour"));

        return response;
    }

    private SignupUserResponse buildUserSignupResponse(User user) {
        return SignupUserResponse.builder()
                .status_code(HttpStatus.CREATED.value())
                .status("success")
                .message("User registered successfully")
                .data(SignupUserData.builder()
                        .id(user.getId())
                        .profilePicture(user.getProfilePicture())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .isVerified(user.isVerified())
                        .isActive(user.isActive())
                        .role(user.getRole())
                        .lastLogin(user.getLastLogin())
                        .build())
                .build();
    }

    private UserLoginResponse buildUserLoginResponse(User user, String accessToken) {
        return UserLoginResponse.builder()
                .status_code(HttpStatus.OK.value())
                .status("success")
                .message("User logged in successfully")
                .data(LoginUserData.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .isVerified(user.isVerified())
                        .isActive(user.isActive())
                        .role(user.getRole())
                        .lastLogin(user.getLastLogin())
                        .accessToken(accessToken)
                        .createdAt(user.getCreatedAt())
                        .updatedAt(user.getUpdatedAt())
                        .build())
                .build();
    }
}