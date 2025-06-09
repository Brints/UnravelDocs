package com.extractor.unraveldocs.auth.service.impl;

import com.extractor.unraveldocs.auth.dto.SignupData;
import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
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
import com.extractor.unraveldocs.utils.imageupload.cloudinary.CloudinaryService;
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
    private ResponseBuilderService responseBuilder;

    @Mock
    private CloudinaryService cloudinaryService;

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

    private SignUpRequestDto request;
    private LocalDateTime expiryDate;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        expiryDate = now.plusHours(3);

        request = new SignUpRequestDto(
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

        SignupData data = SignupData.builder()
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

        UserResponse<SignupData> expectedResponse = new UserResponse<>();
        expectedResponse.setStatusCode(HttpStatus.CREATED.value());
        expectedResponse.setStatus("success");
        expectedResponse.setMessage("User registered successfully");
        expectedResponse.setData(data);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName("john")).thenReturn("John");
        when(userLibrary.capitalizeFirstLetterOfName("doe")).thenReturn("Doe");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("verificationToken");
        when(dateHelper.setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3))).thenReturn(expiryDate);
        when(dateHelper.getTimeLeftToExpiry(any(LocalDateTime.class), any(LocalDateTime.class), eq("hour"))).thenReturn("3");
        when(userRepository.superAdminExists()).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(String.valueOf(1L));
            return savedUser;
        });
        when(responseBuilder.buildUserResponse(
                any(SignupData.class), eq(HttpStatus.CREATED), eq("User registered successfully")
        )).thenReturn(expectedResponse);

        // Act
        UserResponse<SignupData> response = signupUserService.registerUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals("success", response.getStatus());
        assertEquals("User registered successfully", response.getMessage());
        assertNotNull(response.getData());
        assertEquals("John", response.getData().firstName());
        assertEquals("Doe", response.getData().lastName());
        assertEquals("john.doe@example.com", response.getData().email());
        assertEquals(Role.USER, response.getData().role());
        assertFalse(response.getData().isVerified());
        assertFalse(response.getData().isActive());
        assertNull(response.getData().lastLogin());
        assertNull(response.getData().profilePicture());

        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userLibrary).capitalizeFirstLetterOfName("john");
        verify(userLibrary).capitalizeFirstLetterOfName("doe");
        verify(passwordEncoder).encode("P@ssw0rd123");
        verify(verificationToken).generateVerificationToken();
        verify(dateHelper).setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3));
        verify(templatesService).sendVerificationEmail(
                eq("john.doe@example.com"), eq("John"), eq("Doe"), eq("verificationToken"), eq("3"));
        verify(userRepository).save(any(User.class));
        verify(responseBuilder).buildUserResponse(
                any(SignupData.class), eq(HttpStatus.CREATED), eq("User registered successfully")
        );
        verifyNoInteractions(cloudinaryService);
    }

    @Test
    void registerUser_FirstUser_SetsSuperAdminRole() {
        // Arrange
        User user = new User();
        user.setId(String.valueOf(1L));
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setProfilePicture(null);
        user.setVerified(false);
        user.setActive(false);
        user.setRole(Role.SUPER_ADMIN);
        user.setLastLogin(null);

        SignupData data = SignupData.builder()
                .id(String.valueOf(1L))
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .profilePicture(null)
                .isVerified(false)
                .isActive(false)
                .role(Role.SUPER_ADMIN)
                .lastLogin(null)
                .build();

        UserResponse<SignupData> expectedResponse = new UserResponse<>();
        expectedResponse.setStatusCode(HttpStatus.CREATED.value());
        expectedResponse.setStatus("success");
        expectedResponse.setMessage("User registered successfully");
        expectedResponse.setData(data);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(anyString())).thenReturn("John");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("verificationToken");
        when(dateHelper.setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3))).thenReturn(expiryDate);
        when(dateHelper.getTimeLeftToExpiry(any(LocalDateTime.class), any(LocalDateTime.class), eq("hour"))).thenReturn("3");
        when(userRepository.superAdminExists()).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(String.valueOf(1L));
            return savedUser;
        });
        when(responseBuilder.buildUserResponse(
                any(SignupData.class), eq(HttpStatus.CREATED), eq("User registered successfully")
        )).thenReturn(expectedResponse);

        // Act
        UserResponse<SignupData> response = signupUserService.registerUser(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals(Role.SUPER_ADMIN, response.getData().role());
        verify(userRepository).superAdminExists();
    }

    @Test
    void registerUser_EmailAlreadyExists_ThrowsConflictException() {
        // Arrange
        when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(ConflictException.class, () -> signupUserService.registerUser(request),
                "Email already exists");
        verify(userRepository).existsByEmail("john.doe@example.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(userLibrary, passwordEncoder, verificationToken, dateHelper,
                templatesService, responseBuilder, cloudinaryService);
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
                templatesService, responseBuilder, cloudinaryService);
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
        user.setProfilePicture("https://cloudinary.com/images/profile_pictures/unique-profile.jpg");
        user.setVerified(false);
        user.setActive(false);
        user.setRole(Role.USER);
        user.setLastLogin(null);

        SignupData data = SignupData.builder()
                .id(String.valueOf(1L))
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .profilePicture("https://cloudinary.com/images/profile_pictures/unique-profile.jpg")
                .isVerified(false)
                .isActive(false)
                .role(Role.USER)
                .lastLogin(null)
                .build();

        UserResponse<SignupData> expectedResponse = new UserResponse<>();
        expectedResponse.setStatusCode(HttpStatus.CREATED.value());
        expectedResponse.setStatus("success");
        expectedResponse.setMessage("User registered successfully");
        expectedResponse.setData(data);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(anyString())).thenReturn("John");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("verificationToken");
        when(dateHelper.setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3))).thenReturn(expiryDate);
        when(dateHelper.getTimeLeftToExpiry(any(LocalDateTime.class), any(LocalDateTime.class), eq("hour"))).thenReturn("3");
        when(userRepository.superAdminExists()).thenReturn(false);
        when(cloudinaryService.uploadFile(
                eq(profilePicture), eq("profile_pictures"),
                eq(profilePicture.getOriginalFilename()), eq("image")))
                .thenReturn("https://cloudinary.com/images/profile_pictures/unique-profile.jpg");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(String.valueOf(1L));
            return savedUser;
        });
        when(responseBuilder.buildUserResponse(
                any(SignupData.class), eq(HttpStatus.CREATED), eq("User registered successfully")
        )).thenReturn(expectedResponse);

        // Act
        UserResponse<SignupData> response = signupUserService.registerUser(requestWithPicture);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED.value(), response.getStatusCode());
        assertEquals("https://cloudinary.com/images/profile_pictures/unique-profile.jpg", response.getData().profilePicture());
        verify(cloudinaryService).uploadFile(
                eq(profilePicture), eq("profile_pictures"),
                eq(profilePicture.getOriginalFilename()), eq("image"));
        verify(userRepository).save(argThat(u ->
                u.getProfilePicture().equals("https://cloudinary.com/images/profile_pictures/unique-profile.jpg")));
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
        when(cloudinaryService.uploadFile(
                eq(profilePicture), eq("profile_pictures"),
                eq(profilePicture.getOriginalFilename()), eq("image")))
                .thenThrow(new RuntimeException("Cloudinary upload failed"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> signupUserService.registerUser(requestWithPicture),
                "Failed to upload profile picture");

        // Verify only the expected interactions
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userLibrary, times(2)).capitalizeFirstLetterOfName(anyString());
        verify(passwordEncoder).encode("P@ssw0rd123");
        verify(verificationToken).generateVerificationToken();
        verify(dateHelper).setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3));
        verify(cloudinaryService).uploadFile(
                eq(profilePicture), eq("profile_pictures"),
                eq(profilePicture.getOriginalFilename()), eq("image"));

        // Verify no user was saved
        verify(userRepository, never()).save(any());
        verifyNoInteractions(templatesService, responseBuilder);
    }

    @Test
    void registerUser_VerificationDetailsAndLoginAttemptsSetCorrectly() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(anyString())).thenReturn("John");
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("verificationToken");
        when(dateHelper.setExpiryDate(any(LocalDateTime.class), eq("hour"), eq(3))).thenReturn(expiryDate);
        when(dateHelper.getTimeLeftToExpiry(any(LocalDateTime.class), any(LocalDateTime.class), eq("hour"))).thenReturn("3");
        when(userRepository.superAdminExists()).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(String.valueOf(1L));
            return savedUser;
        });
        when(responseBuilder.buildUserResponse(
                any(SignupData.class), eq(HttpStatus.CREATED), eq("User registered successfully")
        )).thenReturn(any());

        // Act
        signupUserService.registerUser(request);

        // Assert
        verify(userRepository).save(argThat(u -> {
            // Verify UserVerification properties
            UserVerification verification = u.getUserVerification();
            boolean verificationCorrect = verification.getEmailVerificationToken().equals("verificationToken") &&
                    verification.getStatus() == VerifiedStatus.PENDING &&
                    verification.getEmailVerificationTokenExpiry().equals(expiryDate) &&
                    !verification.isEmailVerified() &&
                    verification.getPasswordResetToken() == null &&
                    verification.getPasswordResetTokenExpiry() == null;

            // Verify LoginAttempts is created and associated with the user
            LoginAttempts loginAttempts = u.getLoginAttempts();
            boolean loginAttemptsCorrect = loginAttempts != null &&
                    loginAttempts.getUser() == u;

            return verificationCorrect && loginAttemptsCorrect;
        }));
    }
}