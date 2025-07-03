package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.auth.model.UserVerification;
import com.extractor.unraveldocs.auth.repository.UserVerificationRepository;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.UserEmailTemplateService;
import com.extractor.unraveldocs.user.impl.DeleteUserImpl;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.utils.imageupload.cloudinary.CloudinaryService; // Updated import
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteUserImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserVerificationRepository userVerificationRepository;
    @Mock
    private CloudinaryService cloudinaryService;
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
        User user = new User();
        user.setId("1");
        user.setEmail("test@example.com");
        UserVerification verification = new UserVerification();
        user.setUserVerification(verification);

        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        deleteUserImpl.scheduleUserDeletion("1");

        assertNotNull(user.getDeletedAt());
        assertNotNull(user.getUserVerification().getDeletedAt());
        verify(userEmailTemplateService).scheduleUserDeletion("test@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void scheduleUserDeletion_shouldThrowIfUserNotFound() {
        when(userRepository.findById("2")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> deleteUserImpl.scheduleUserDeletion("2"));
    }

    @Test
    void checkAndScheduleInactiveUsers_shouldScheduleForInactiveUsers() {
        User user = new User();
        user.setId("3");
        user.setEmail("inactive@example.com");
        user.setActive(true);
        UserVerification verification = new UserVerification();
        user.setUserVerification(verification);

        List<User> inactiveUsers = Collections.singletonList(user);
        when(userRepository.findAllByLastLoginDateBefore(any())).thenReturn(inactiveUsers);

        deleteUserImpl.checkAndScheduleInactiveUsers();

        assertFalse(user.isActive());
        assertNotNull(user.getDeletedAt());
        assertNotNull(user.getUserVerification().getDeletedAt());
        verify(userEmailTemplateService).scheduleUserDeletion("inactive@example.com");
        verify(userRepository).saveAll(inactiveUsers);
    }

    @Test
    void processScheduledDeletions_shouldDeleteUsersAndRelatedData() {
        User user = new User();
        user.setId("4");
        user.setProfilePicture("pic.jpg");
        UserVerification verification = new UserVerification();
        user.setUserVerification(verification);

        List<User> usersToDelete = Collections.singletonList(user);
        when(userRepository.findAllByDeletedAtBefore(any())).thenReturn(usersToDelete);

        deleteUserImpl.processScheduledDeletions();

        verify(cloudinaryService).deleteFile("pic.jpg"); // Updated to cloudinaryService
        verify(userVerificationRepository).delete(verification);
        verify(userRepository).deleteAll(usersToDelete);
    }

    @Test
    void deleteUser_shouldDeleteUserAndRelatedData() {
        User user = new User();
        user.setId("5");
        user.setProfilePicture("pic2.jpg");
        UserVerification verification = new UserVerification();
        user.setUserVerification(verification);

        when(userRepository.findById("5")).thenReturn(Optional.of(user));

        deleteUserImpl.deleteUser("5");

        verify(cloudinaryService).deleteFile("pic2.jpg"); // Updated to cloudinaryService
        verify(userVerificationRepository).delete(verification);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteUser_shouldThrowIfUserNotFound() {
        when(userRepository.findById("6")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> deleteUserImpl.deleteUser("6"));
    }
}