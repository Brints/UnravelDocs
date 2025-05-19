package com.extractor.unraveldocs.auth.service.impl;

import com.extractor.unraveldocs.auth.dto.SignupUserData;
import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.dto.response.SignupUserResponse;
import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignupUserImplTest {

    @Mock
    private AuthEmailTemplateService templatesService;

    @Mock
    private AuthResponseBuilderService responseBuilder;

    @Mock
    private AwsS3Service awsS3Service;

    @Mock
    private DateHelper dateHelper;

    @Mock
    private GenerateVerificationToken verificationToken;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserLibrary userLibrary;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SignupUserImpl signupUserService;

    private SignUpRequestDto signUpRequest;
    private LocalDateTime now;
    private LocalDateTime expiryDate;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        expiryDate = now.plusHours(3);

        signUpRequest = new SignUpRequestDto(
                "john",
                "doe",
                "john.doe@example.com",
                "P@ssw0rd123",
                "P@ssw0rd123",
                null
        );
    }

    @Test
    void registerUser_SuccessfulRegistrationWithoutProfilePicture_ReturnsSignupResponse() {
        // Arrange
        User user = new User();
        user.setId(String.valueOf(1L));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setProfilePicture(null);
        user.setVerified(false);
        user.setActive(false);
        user.setRole(Role.USER);
        user.setLastLogin(null);

        SignupUserData userData = SignupUserData.builder()
                .id(String.valueOf(1L))
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .profilePicture(null)
                .isVerified(false)
                .isActive(false)
                .role(Role.USER)
                .lastLogin(null)
                .build();

        SignupUserResponse expectedResponse = SignupUserResponse.builder()
                .statusCode(HttpStatus.CREATED.value())
                .status("success")
                .message("User registered successfully")
                .data(userData)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName("john")).thenReturn("John");
        when(userLibrary.capitalizeFirstLetterOfName("doe")).thenReturn("Doe");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("verificationToken");
        when(dateHelper.setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3))).thenReturn(expiryDate);
        when(dateHelper.getTimeLeftToExpiry(any(LocalDateTime.class), any(LocalDateTime.class), eq("hour"))).thenReturn("3");
        when(userRepository.isFirstUserWithLock()).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(String.valueOf(1L));
            return savedUser;
        });
        when(responseBuilder.buildUserSignupResponse(any(User.class))).thenReturn(expectedResponse);

        // Act
        SignupUserResponse response = signupUserService.registerUser(signUpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.value(), response.statusCode());
        assertEquals("success", response.status());
        assertEquals("User registered successfully", response.message());
        assertNotNull(response.data());
        assertEquals("John", response.data().firstName());
        assertEquals("Doe", response.data().lastName());
        assertEquals("john.doe@example.com", response.data().email());
        assertEquals(Role.USER, response.data().role());
        assertFalse(response.data().isVerified());
        assertFalse(response.data().isActive());
        assertNull(response.data().lastLogin());
        assertNull(response.data().profilePicture());

        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userLibrary).capitalizeFirstLetterOfName("john");
        verify(userLibrary).capitalizeFirstLetterOfName("doe");
        verify(passwordEncoder).encode("P@ssw0rd123");
        verify(verificationToken).generateVerificationToken();
        verify(dateHelper).setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3));
        verify(templatesService).sendVerificationEmail(
                eq("john.doe@example.com"), eq("John"), eq("Doe"), eq("verificationToken"), eq("3"));
        verify(userRepository).save(any(User.class));
        verify(responseBuilder).buildUserSignupResponse(any(User.class));
        verifyNoInteractions(awsS3Service);
    }

    @Test
    void registerUser_FirstUser_SetsAdminRole() {
        // Arrange
        User user = new User();
        user.setId(String.valueOf(1L));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setProfilePicture(null);
        user.setVerified(false);
        user.setActive(false);
        user.setRole(Role.ADMIN);
        user.setLastLogin(null);

        SignupUserData userData = SignupUserData.builder()
                .id(String.valueOf(1L))
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .profilePicture(null)
                .isVerified(false)
                .isActive(false)
                .role(Role.ADMIN)
                .lastLogin(null)
                .build();

        SignupUserResponse expectedResponse = SignupUserResponse.builder()
                .statusCode(HttpStatus.CREATED.value())
                .status("success")
                .message("User registered successfully")
                .data(userData)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(anyString())).thenReturn("John");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("verificationToken");
        when(dateHelper.setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3))).thenReturn(expiryDate);
        when(dateHelper.getTimeLeftToExpiry(any(LocalDateTime.class), any(LocalDateTime.class), eq("hour"))).thenReturn("3");
        when(userRepository.isFirstUserWithLock()).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(String.valueOf(1L));
            return savedUser;
        });
        when(responseBuilder.buildUserSignupResponse(any(User.class))).thenReturn(expectedResponse);

        // Act
        SignupUserResponse response = signupUserService.registerUser(signUpRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.value(), response.statusCode());
        assertEquals(Role.ADMIN, response.data().role());
        verify(userRepository).isFirstUserWithLock();
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsConflictException() {
        // Arrange
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> signupUserService.registerUser(signUpRequest),
                "Email already exists");
        verify(userRepository).existsByEmail("john.doe@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userLibrary, passwordEncoder, verificationToken, dateHelper,
                templatesService, responseBuilder, awsS3Service);
    }

    @Test
    void registerUser_PasswordSameAsEmail_ThrowsBadRequestException() {
        // Arrange
        SignUpRequestDto invalidRequest = new SignUpRequestDto(
                "john",
                "doe",
                "john.doe@example.com",
                "john.doe@example.com",
                "john.doe@example.com",
                null
        );
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(BadRequestException.class, () -> signupUserService.registerUser(invalidRequest),
                "Password cannot be same as email.");
        verify(userRepository).existsByEmail("john.doe@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userLibrary, passwordEncoder, verificationToken, dateHelper,
                templatesService, responseBuilder, awsS3Service);
    }

    @Test
    void registerUser_WithProfilePicture_SuccessfulUpload() {
        // Arrange
        MultipartFile profilePicture = new MockMultipartFile(
                "profilePicture",
                "profile.jpg",
                "image/jpeg",
                "test image".getBytes()
        );
        SignUpRequestDto requestWithPicture = new SignUpRequestDto(
                "john",
                "doe",
                "john.doe@example.com",
                "P@ssw0rd123",
                "P@ssw0rd123",
                profilePicture
        );

        User user = new User();
        user.setId(String.valueOf(1L));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setProfilePicture("https://s3.amazonaws.com/bucket/profile_pictures/unique-profile.jpg");
        user.setVerified(false);
        user.setActive(false);
        user.setRole(Role.USER);
        user.setLastLogin(null);

        SignupUserData userData = SignupUserData.builder()
                .id(String.valueOf(1L))
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .profilePicture("https://s3.amazonaws.com/bucket/profile_pictures/unique-profile.jpg")
                .isVerified(false)
                .isActive(false)
                .role(Role.USER)
                .lastLogin(null)
                .build();

        SignupUserResponse expectedResponse = SignupUserResponse.builder()
                .statusCode(HttpStatus.CREATED.value())
                .status("success")
                .message("User registered successfully")
                .data(userData)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(anyString())).thenReturn("John");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("verificationToken");
        when(dateHelper.setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3))).thenReturn(expiryDate);
        when(dateHelper.getTimeLeftToExpiry(any(LocalDateTime.class), any(LocalDateTime.class), eq("hour"))).thenReturn("3");
        when(userRepository.isFirstUserWithLock()).thenReturn(false);
        when(awsS3Service.generateFileName("profile.jpg")).thenReturn("profile_pictures/unique-profile.jpg");
        when(awsS3Service.uploadFile(profilePicture, "profile_pictures/unique-profile.jpg"))
                .thenReturn("https://s3.amazonaws.com/bucket/profile_pictures/unique-profile.jpg");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(String.valueOf(1L));
            return savedUser;
        });
        when(responseBuilder.buildUserSignupResponse(any(User.class))).thenReturn(expectedResponse);

        // Act
        SignupUserResponse response = signupUserService.registerUser(requestWithPicture);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.value(), response.statusCode());
        assertEquals("https://s3.amazonaws.com/bucket/profile_pictures/unique-profile.jpg", response.data().profilePicture());
        verify(awsS3Service).generateFileName("profile.jpg");
        verify(awsS3Service).uploadFile(profilePicture, "profile_pictures/unique-profile.jpg");
        verify(userRepository).save(argThat(u ->
                u.getProfilePicture().equals("https://s3.amazonaws.com/bucket/profile_pictures/unique-profile.jpg")));
    }

    @Test
    void registerUser_ProfilePictureUploadFails_ThrowsBadRequestException() {
        // Arrange
        MultipartFile profilePicture = new MockMultipartFile(
                "profilePicture",
                "profile.jpg",
                "image/jpeg",
                "test image".getBytes()
        );
        SignUpRequestDto requestWithPicture = new SignUpRequestDto(
                "john",
                "doe",
                "john.doe@example.com",
                "P@ssw0rd123",
                "P@ssw0rd123",
                profilePicture
        );

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(anyString())).thenReturn("John", "Doe");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("verificationToken");
        when(dateHelper.setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3))).thenReturn(expiryDate);
        when(awsS3Service.generateFileName("profile.jpg")).thenReturn("profile_pictures/unique-profile.jpg");
        when(awsS3Service.uploadFile(any(), anyString())).thenThrow(new RuntimeException("S3 upload failed"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> signupUserService.registerUser(requestWithPicture),
                "Failed to upload profile picture");

        // Verify only the expected interactions
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userLibrary, times(2)).capitalizeFirstLetterOfName(anyString());
        verify(passwordEncoder).encode("P@ssw0rd123");
        verify(verificationToken).generateVerificationToken();
        verify(dateHelper).setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3));
        verify(awsS3Service).generateFileName("profile.jpg");
        verify(awsS3Service).uploadFile(profilePicture, "profile_pictures/unique-profile.jpg");

        // Verify no user was saved
        verify(userRepository, never()).save(any());
        verifyNoInteractions(templatesService, responseBuilder);
    }

    @Test
    void registerUser_VerificationDetailsSetCorrectly() {
        // Arrange
        User user = new User();
        user.setId(String.valueOf(1L));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setProfilePicture(null);
        user.setVerified(false);
        user.setActive(false);
        user.setRole(Role.USER);
        user.setLastLogin(null);

        SignupUserData userData = SignupUserData.builder()
                .id(String.valueOf(1L))
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .profilePicture(null)
                .isVerified(false)
                .isActive(false)
                .role(Role.USER)
                .lastLogin(null)
                .build();

        SignupUserResponse expectedResponse = SignupUserResponse.builder()
                .statusCode(HttpStatus.CREATED.value())
                .status("success")
                .message("User registered successfully")
                .data(userData)
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(anyString())).thenReturn("John");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("verificationToken");
        when(dateHelper.setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3))).thenReturn(expiryDate);
        when(dateHelper.getTimeLeftToExpiry(any(LocalDateTime.class), any(LocalDateTime.class), eq("hour"))).thenReturn("3");
        when(userRepository.isFirstUserWithLock()).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(String.valueOf(1L));
            return savedUser;
        });
        when(responseBuilder.buildUserSignupResponse(any(User.class))).thenReturn(expectedResponse);

        // Act
        signupUserService.registerUser(signUpRequest);

        // Assert
        verify(userRepository).save(argThat(u -> {
            UserVerification verification = u.getUserVerification();
            return verification.getEmailVerificationToken().equals("verificationToken") &&
                    verification.getStatus() == VerifiedStatus.PENDING &&
                    verification.getEmailVerificationTokenExpiry().equals(expiryDate) &&
                    !verification.isEmailVerified() &&
                    verification.getPasswordResetToken() == null &&
                    verification.getPasswordResetTokenExpiry() == null;
        }));
    }
}