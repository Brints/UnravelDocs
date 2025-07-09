package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.auth.model.UserVerification;
import com.extractor.unraveldocs.auth.repository.UserVerificationRepository;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.UserEmailTemplateService;
import com.extractor.unraveldocs.user.impl.DeleteUserImpl;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.utils.imageupload.aws.AwsS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DeleteUserImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserVerificationRepository userVerificationRepository;

    @Mock
    private AwsS3Service awsS3Service;

    @Mock
    private UserEmailTemplateService userEmailTemplateService;

    @InjectMocks
    private DeleteUserImpl deleteUserImpl;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void scheduleUserDeletion_shouldSetDeletedAtAndSendEmail() {
        // Arrange
        User user = new User();
        user.setId("1");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        UserVerification verification = new UserVerification();
        user.setUserVerification(verification);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        // Act
        deleteUserImpl.scheduleUserDeletion("1");

        // Assert
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();

        assertNotNull(savedUser.getDeletedAt());
        assertNotNull(savedUser.getUserVerification().getDeletedAt());
        assertEquals(savedUser.getDeletedAt(), savedUser.getUserVerification().getDeletedAt());

        verify(userEmailTemplateService).scheduleUserDeletion(
                eq("test@example.com"),
                eq("Test"),
                eq("User"),
                any(OffsetDateTime.class)
        );
    }

    @Test
    void scheduleUserDeletion_shouldThrowIfUserNotFound() {
        // Arrange
        when(userRepository.findById("2")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> deleteUserImpl.scheduleUserDeletion("2"));
    }

    @Test
    void checkAndScheduleInactiveUsers_shouldScheduleForInactiveUsers() {
        // Arrange
        User user = new User();
        user.setId("3");
        user.setEmail("inactive@example.com");
        user.setFirstName("Inactive");
        user.setLastName("User");
        user.setActive(true);
        UserVerification verification = new UserVerification();
        user.setUserVerification(verification);

        List<User> inactiveUsers = Collections.singletonList(user);
        Page<User> inactiveUsersPage = new PageImpl<>(inactiveUsers, PageRequest.of(0, 100), 1);

        when(userRepository.findAllByLastLoginDateBefore(any(OffsetDateTime.class), any(Pageable.class)))
                .thenReturn(inactiveUsersPage)
                .thenReturn(Page.empty()); // Return empty page for subsequent calls to terminate loop

        // Act
        deleteUserImpl.checkAndScheduleInactiveUsers();

        // Assert
        assertFalse(user.isActive());
        assertNotNull(user.getDeletedAt());
        assertNotNull(user.getUserVerification().getDeletedAt());
        verify(userEmailTemplateService).scheduleUserDeletion(
                eq("inactive@example.com"),
                eq("Inactive"),
                eq("User"),
                any(OffsetDateTime.class)
        );
        verify(userRepository).saveAll(inactiveUsers);
    }

    @Test
    void processScheduledDeletions_shouldDeleteUsersAndRelatedData() {
        // Arrange
        User user = new User();
        user.setId("4");
        user.setProfilePicture("pic.jpg");
        UserVerification verification = new UserVerification();
        user.setUserVerification(verification);

        List<User> usersToDelete = Collections.singletonList(user);
        Page<User> usersToDeletePage = new PageImpl<>(usersToDelete, PageRequest.of(0, 100), 1);

        when(userRepository.findAllByDeletedAtBefore(any(OffsetDateTime.class), any(Pageable.class)))
                .thenReturn(usersToDeletePage)
                .thenReturn(Page.empty());

        // Act
        deleteUserImpl.processScheduledDeletions();

        // Assert
        verify(awsS3Service).deleteFile("pic.jpg");
        verify(userVerificationRepository).delete(verification);
        verify(userRepository).deleteAll(usersToDelete);
    }

    @Test
    void deleteUser_shouldSendEmailThenDeleteUserAndRelatedData() {
        // Arrange
        User user = new User();
        user.setId("5");
        user.setEmail("deleted@example.com");
        user.setFirstName("Deleted");
        user.setProfilePicture("pic2.jpg");
        UserVerification verification = new UserVerification();
        user.setUserVerification(verification);

        when(userRepository.findById("5")).thenReturn(Optional.of(user));

        // Act
        deleteUserImpl.deleteUser("5");

        // Assert
        InOrder inOrder = inOrder(userEmailTemplateService, awsS3Service, userVerificationRepository, userRepository);

        inOrder.verify(userEmailTemplateService).sendDeletedAccountEmail("deleted@example.com");
        inOrder.verify(awsS3Service).deleteFile("pic2.jpg");
        inOrder.verify(userVerificationRepository).delete(verification);
        inOrder.verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowIfUserNotFound() {
        // Arrange
        when(userRepository.findById("6")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> deleteUserImpl.deleteUser("6"));
    }
}