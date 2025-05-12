package com.extractor.unraveldocs.auth;

import com.extractor.unraveldocs.auth.dto.request.SignUpRequestDto;
import com.extractor.unraveldocs.auth.dto.response.SignupUserResponse;
import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.auth.enums.VerifiedStatus;
import com.extractor.unraveldocs.auth.model.UserVerification;
import com.extractor.unraveldocs.auth.service.AuthResponseBuilderService;
import com.extractor.unraveldocs.auth.service.impl.SignupUserImpl;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SignupUserImplTest {
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
    private SignupUserImpl signupUserImpl;

    private SignUpRequestDto validRequest;
    private SignUpRequestDto requestWithProfilePicture;
    private SignUpRequestDto requestWithPasswordSameAsEmail;
    private User savedUser;
    private UserVerification userVerification;

    @BeforeEach
    void setUp() {
        validRequest = new SignUpRequestDto("john", "doe", "john.doe@example.com", "p@ssword123", "p@ssword123", null);

        requestWithProfilePicture = new SignUpRequestDto("jane", "doe", "jane.doe@example.com", "p@ssword123", "p@ssword123", mock(MultipartFile.class));

        requestWithPasswordSameAsEmail = new SignUpRequestDto("john", "doe", "john.doe@example.com", "john.doe@example.com", "john.doe@example.com", null);

        userVerification = new UserVerification();
        userVerification.setEmailVerificationToken("token123");
        userVerification.setStatus(VerifiedStatus.PENDING);
        userVerification.setEmailVerificationTokenExpiry(LocalDateTime.now().plusHours(3));
        userVerification.setEmailVerified(false);
        userVerification.setPasswordResetToken(null);
        userVerification.setPasswordResetTokenExpiry(null);

        savedUser = new User();
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setEmail("john.doe@example.com");
        savedUser.setPassword("encryptedPassword");
        savedUser.setProfilePicture("profilePictureUrl");
        savedUser.setUserVerification(userVerification);
        savedUser.setRole(Role.USER);
        savedUser.setActive(false);
        savedUser.setVerified(false);
        savedUser.setLastLogin(null);
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(validRequest.firstName())).thenReturn("John");
        when(userLibrary.capitalizeFirstLetterOfName(validRequest.lastName())).thenReturn("Doe");
        when(passwordEncoder.encode(validRequest.password())).thenReturn("encryptedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("token123");
        when(dateHelper.setExpiryDate(LocalDateTime.now(), anyString(), anyInt())).thenReturn(LocalDateTime.now().plusHours(3));
        when(userRepository.isFirstUserWithLock()).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(responseBuilder.buildUserSignupResponse(any(User.class))).thenReturn(mock(SignupUserResponse.class));

        SignupUserResponse response = signupUserImpl.registerUser(validRequest);

        assertNotNull(response);
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(userRepository).save(any(User.class));
        verify(templatesService).sendVerificationEmail(
                eq("john.doe@example.com"),
                eq("John"),
                eq("Doe"),
                eq("token123"),
                anyString()
        );
    }

    @Test
    void testRegisterUser_Success_WithProfilePicture() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(requestWithProfilePicture.firstName())).thenReturn("Jane");
        when(userLibrary.capitalizeFirstLetterOfName(requestWithProfilePicture.lastName())).thenReturn("Doe");
        when(passwordEncoder.encode(requestWithProfilePicture.password())).thenReturn("encryptedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("token123");
        when(dateHelper.setExpiryDate(LocalDateTime.now(), anyString(), anyInt())).thenReturn(LocalDateTime.now().plusHours(3));
        when(userRepository.isFirstUserWithLock()).thenReturn(false);
        when(awsS3Service.generateFileName(anyString())).thenReturn("fileName");
        when(awsS3Service.uploadFile(any(MultipartFile.class), anyString())).thenReturn("https://example.com/profile.jpg");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(responseBuilder.buildUserSignupResponse(any(User.class))).thenReturn(mock(SignupUserResponse.class));

        SignupUserResponse response = signupUserImpl.registerUser(requestWithProfilePicture);

        assertNotNull(response);
        verify(awsS3Service).uploadFile(any(MultipartFile.class), eq("fileName"));
    }

    @Test
    void testRegisterUser_PasswordSameAsEmail() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        assertThrows(BadRequestException.class, () -> {
            signupUserImpl.registerUser(requestWithPasswordSameAsEmail);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_EmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> {
            signupUserImpl.registerUser(validRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_ProfilePictureUploadError() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(requestWithProfilePicture.firstName())).thenReturn("Jane");
        when(userLibrary.capitalizeFirstLetterOfName(requestWithProfilePicture.lastName())).thenReturn("Doe");
        when(passwordEncoder.encode(requestWithProfilePicture.password())).thenReturn("encryptedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("token123");
        when(dateHelper.setExpiryDate(LocalDateTime.now(), anyString(), anyInt())).thenReturn(LocalDateTime.now().plusHours(3));
        when(userRepository.isFirstUserWithLock()).thenReturn(false);
        when(awsS3Service.generateFileName(anyString())).thenReturn("fileName");
        when(awsS3Service.uploadFile(any(MultipartFile.class), anyString())).thenThrow(new BadRequestException("Failed to upload profile picture"));

        assertThrows(BadRequestException.class, () -> {
            signupUserImpl.registerUser(requestWithProfilePicture);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUser_FirstUser_AssignsAdminRole() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(validRequest.firstName())).thenReturn("John");
        when(userLibrary.capitalizeFirstLetterOfName(validRequest.lastName())).thenReturn("Doe");
        when(passwordEncoder.encode(validRequest.password())).thenReturn("encryptedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("token123");
        when(dateHelper.setExpiryDate(LocalDateTime.now(), anyString(), anyInt())).thenReturn(LocalDateTime.now().plusHours(3));
        when(userRepository.isFirstUserWithLock()).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals(Role.ADMIN, user.getRole());
            return user;
        });
        when(responseBuilder.buildUserSignupResponse(any(User.class))).thenReturn(mock(SignupUserResponse.class));

        signupUserImpl.registerUser(validRequest);

        verify(userRepository).isFirstUserWithLock();
    }

    @Test
    void testRegisterUser_CapitalizeFirstLetterOfName() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(validRequest.firstName())).thenReturn("John");
        when(userLibrary.capitalizeFirstLetterOfName(validRequest.lastName())).thenReturn("Doe");
        when(passwordEncoder.encode(validRequest.password())).thenReturn("encryptedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("token123");
        when(dateHelper.setExpiryDate(LocalDateTime.now(), anyString(), anyInt())).thenReturn(LocalDateTime.now().plusHours(3));
        when(userRepository.isFirstUserWithLock()).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("John", user.getFirstName());
            assertEquals("Doe", user.getLastName());
            return user;
        });
        when(responseBuilder.buildUserSignupResponse(any(User.class))).thenReturn(mock(SignupUserResponse.class));

        signupUserImpl.registerUser(validRequest);

        verify(userLibrary, times(2)).capitalizeFirstLetterOfName(anyString());
    }

    @Test
    void testRegisterUser_EmailStoredInLowerCase() {
        SignUpRequestDto mixedCaseRequest = new SignUpRequestDto("John", "Doe", "John.Doe@Example.com", "p@ssword123", "p@ssword123", null);

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userLibrary.capitalizeFirstLetterOfName(validRequest.firstName())).thenReturn("John");
        when(userLibrary.capitalizeFirstLetterOfName(validRequest.lastName())).thenReturn("Doe");
        when(passwordEncoder.encode(validRequest.password())).thenReturn("encryptedPassword");
        when(verificationToken.generateVerificationToken()).thenReturn("token123");
        when(dateHelper.setExpiryDate(LocalDateTime.now(), anyString(), anyInt())).thenReturn(LocalDateTime.now().plusHours(3));
        when(userRepository.isFirstUserWithLock()).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("john.doe@example.com", user.getEmail());
            return user;
        });
        when(responseBuilder.buildUserSignupResponse(any(User.class))).thenReturn(mock(SignupUserResponse.class));

        signupUserImpl.registerUser(mixedCaseRequest);
        verify(userRepository).save(argThat(user -> user.getEmail().equals("john.doe@example.com")));
    }
}
