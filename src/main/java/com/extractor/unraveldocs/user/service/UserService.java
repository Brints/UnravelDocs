package com.extractor.unraveldocs.user.service;

import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
import com.extractor.unraveldocs.auth.model.UserVerification;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.user.dto.UserData;
import com.extractor.unraveldocs.user.dto.request.ForgotPasswordDto;
import com.extractor.unraveldocs.user.dto.request.ResetPasswordDto;
import com.extractor.unraveldocs.user.dto.response.UserResponse;
import com.extractor.unraveldocs.user.interfaces.IPasswordReset;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.utils.generatetoken.GenerateVerificationToken;
import com.extractor.unraveldocs.utils.userlib.DateHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GenerateVerificationToken generateVerificationToken;
    private final DateHelper dateHelper;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserProfileById(String userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return buildUserResponse(user);
    }

    public UserResponse getAuthenticatedUserProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return buildUserResponse(user);
    }

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
            String timeLeft = dateHelper.getTimeLeftToExpiry(userVerification.getPasswordResetTokenExpiry(), "hours");
            throw new BadRequestException(
                    "A password reset request has already been sent. Token expires in : " + timeLeft);
        }

        String token = generateVerificationToken.generateVerificationToken();
        LocalDateTime expiryTime = dateHelper.setExpiryDate("hour", 1);

        userVerification.setPasswordResetToken(token);
        userVerification.setPasswordResetTokenExpiry(expiryTime);
        userRepository.save(user);

        // Send email with the token (implementation not shown)

        return buildResponseWithoutData("Password reset link sent to your email.");
    }

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

        return buildResponseWithoutData("Password reset successfully.");
    }

    private UserResponse buildUserResponse(User user) {
        UserData userData = new UserData();
        userData.setId(user.getId());
        userData.setProfilePicture(user.getProfilePicture());
        userData.setFirstName(user.getFirstName());
        userData.setLastName(user.getLastName());
        userData.setEmail(user.getEmail());
        userData.setRole(user.getRole());
        userData.setLastLogin(user.getLastLogin());
        userData.setVerified(user.isVerified());
        userData.setCreatedAt(user.getCreatedAt());
        userData.setUpdatedAt(user.getUpdatedAt());

        UserResponse userResponse = new UserResponse();
        userResponse.setStatusCode(HttpStatus.OK.value());
        userResponse.setStatus("success");
        userResponse.setMessage("User profile retrieved successfully");
        userResponse.setData(userData);

        return userResponse;
    }

    private UserResponse buildResponseWithoutData(String message) {
        UserResponse userResponse = new UserResponse();
        userResponse.setStatusCode(HttpStatus.OK.value());
        userResponse.setStatus("success");
        userResponse.setMessage(message);
        userResponse.setData(null);

        return userResponse;
    }
}
