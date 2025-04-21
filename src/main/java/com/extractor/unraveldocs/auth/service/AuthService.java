package com.extractor.unraveldocs.auth.service;

import com.extractor.unraveldocs.auth.dto.UserData;
import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.dto.response.UserResponseDto;
import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
import com.extractor.unraveldocs.auth.model.UserVerification;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ConflictException;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.utils.generatetoken.GenerateVerificationToken;
import com.extractor.unraveldocs.utils.userlib.DateHelper;
import com.extractor.unraveldocs.utils.userlib.UserLibrary;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserLibrary userLibrary;
    private final GenerateVerificationToken verificationToken;
    private final DateHelper dateHelper;

    @Override
    public UserDetails loadUserByUsername(String email) throws BadRequestException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!user.isVerified()) {
            throw new BadRequestException("User is not yet verified. Please check your email for verification.");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().toString())
                .build();
    }

    @Transactional
    public UserResponseDto registerUser(SignUpRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already exists");
        }

        if (request.password().equalsIgnoreCase(request.email())) {
            throw new BadRequestException("Password cannot be same as email.");
        }

        String transformedFirstName = userLibrary.capitalizeFirstLetterOfName(request.firstName());
        String transformedLastName = userLibrary.capitalizeFirstLetterOfName(request.lastName());

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

        User user = new User();
        user.setEmail(request.email().toLowerCase());
        user.setPassword(encryptedPassword);
        user.setFirstName(transformedFirstName);
        user.setLastName(transformedLastName);
        user.setActive(false);
        user.setVerified(false);
        user.setRole(userCount ? Role.ADMIN : Role.USER);
        user.setProfilePicture(null);
        user.setProfilePictureThumbnailUrl(null);
        user.setLastLogin(null);
        user.setUserVerification(userVerification);

        userRepository.save(user);

        return UserResponseDto.builder()
                .status_code(HttpStatus.CREATED.value())
                .status("success")
                .message("User registered successfully")
                .data(UserData.builder()
                        .id(user.getId())
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
}