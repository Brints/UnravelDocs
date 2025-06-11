package com.extractor.unraveldocs.loginattempts.service;

import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.loginattempts.model.LoginAttempts;
import com.extractor.unraveldocs.loginattempts.repository.LoginAttemptsRepository;
import com.extractor.unraveldocs.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginAttemptsImplTest {

    @Mock
    private LoginAttemptsRepository loginAttemptsRepository;

    @InjectMocks
    private LoginAttemptsImpl loginAttemptsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("1");
        user.setEmail("test@example.com");
    }

    @Test
    void checkIfUserBlocked_whenUserNotFound_shouldNotThrowException() {
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> loginAttemptsService.checkIfUserBlocked(user));
    }

    @Test
    void checkIfUserBlocked_whenUserNotBlocked_shouldNotThrowException() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setBlocked(false);
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));
        assertDoesNotThrow(() -> loginAttemptsService.checkIfUserBlocked(user));
    }

    @Test
    void checkIfUserBlocked_whenUserBlockedAndBlockExpired_shouldNotThrowException() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setBlocked(true);
        attempts.setBlockedUntil(LocalDateTime.now().minusDays(1));
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));
        assertDoesNotThrow(() -> loginAttemptsService.checkIfUserBlocked(user));
    }

    @Test
    void checkIfUserBlocked_whenUserBlocked_DaysRemaining_shouldThrowForbiddenException() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setBlocked(true);
        attempts.setBlockedUntil(LocalDateTime.now().plusDays(2).plusHours(1));
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> loginAttemptsService.checkIfUserBlocked(user));
        String expectedMessagePart = "Your account is temporarily locked for 2 days due to multiple failed login attempts.";
        assertTrue(exception.getMessage().contains(expectedMessagePart),
                "Exception message was: " + exception.getMessage() + ", expected to contain: " + expectedMessagePart);
    }

    @Test
    void checkIfUserBlocked_whenUserBlocked_HoursRemaining_shouldThrowForbiddenException() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setBlocked(true);
        attempts.setBlockedUntil(LocalDateTime.now().plusHours(3).plusMinutes(10));
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> loginAttemptsService.checkIfUserBlocked(user));
        String expectedMessagePart = "Your account is temporarily for locked 3 hours due to multiple failed login attempts.";
        assertTrue(exception.getMessage().contains(expectedMessagePart),
                "Exception message was: " + exception.getMessage() + ", expected to contain: " + expectedMessagePart);
    }

    @Test
    void checkIfUserBlocked_whenUserBlocked_MinutesRemaining_shouldThrowForbiddenException() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setBlocked(true);
        attempts.setBlockedUntil(LocalDateTime.now().plusMinutes(30).plusSeconds(10));
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> loginAttemptsService.checkIfUserBlocked(user));
        String expectedMessagePart = "Your account is temporarily locked for 30 minutes due to multiple failed login attempts.";
        assertTrue(exception.getMessage().contains(expectedMessagePart),
                "Exception message was: " + exception.getMessage() + ", expected to contain: " + expectedMessagePart);
    }

    @Test
    void checkIfUserBlocked_whenUserBlocked_LessThanOneMinuteRemaining_shouldThrowForbiddenExceptionWithOneMinute() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setBlocked(true);
        attempts.setBlockedUntil(LocalDateTime.now().plusSeconds(30));
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> loginAttemptsService.checkIfUserBlocked(user));
        String expectedMessagePart = "Your account is temporarily locked for 1 minute due to multiple failed login attempts.";
        assertTrue(exception.getMessage().contains(expectedMessagePart),
                "Exception message was: " + exception.getMessage() + ", expected to contain: " + expectedMessagePart);
    }

    @Test
    void checkIfUserBlocked_whenUserBlocked_ZeroOrNegativeSecondsRemaining_shouldThrowForbiddenExceptionWithOneMinute() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setBlocked(true);
        // Set blockedUntil to be slightly in the future (e.g., 50 milliseconds)
        // This should ensure isAfter(now) is true in checkIfUserBlocked,
        // and duration.getSeconds() will be 0, hitting the desired condition.
        attempts.setBlockedUntil(LocalDateTime.now().plus(50, ChronoUnit.MILLIS));
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> loginAttemptsService.checkIfUserBlocked(user));
        String expectedMessagePart = "Your account is temporarily locked for 1 minute due to multiple failed login attempts.";
        assertTrue(exception.getMessage().contains(expectedMessagePart),
                "Exception message was: " + exception.getMessage() + ", expected to contain: " + expectedMessagePart);
    }


    @Test
    void recordFailedLoginAttempt_newUser_shouldCreateRecordAndThrowException() {
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.empty());
        ArgumentCaptor<LoginAttempts> attemptsCaptor = ArgumentCaptor.forClass(LoginAttempts.class);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> loginAttemptsService.recordFailedLoginAttempt(user));

        verify(loginAttemptsRepository).save(attemptsCaptor.capture());
        LoginAttempts savedAttempts = attemptsCaptor.getValue();
        assertEquals(1, savedAttempts.getLoginAttempts());
        assertFalse(savedAttempts.isBlocked());
        assertNull(savedAttempts.getBlockedUntil());
        assertEquals(user, savedAttempts.getUser());
        assertTrue(exception.getMessage().contains("Invalid credentials. You have 3 attempts left."));
    }

    @Test
    void recordFailedLoginAttempt_existingUser_belowMaxAttempts_shouldIncrementAndThrowException() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setLoginAttempts(1);
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));
        ArgumentCaptor<LoginAttempts> attemptsCaptor = ArgumentCaptor.forClass(LoginAttempts.class);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> loginAttemptsService.recordFailedLoginAttempt(user));

        verify(loginAttemptsRepository).save(attemptsCaptor.capture());
        LoginAttempts savedAttempts = attemptsCaptor.getValue();
        assertEquals(2, savedAttempts.getLoginAttempts());
        assertFalse(savedAttempts.isBlocked());
        assertTrue(exception.getMessage().contains("Invalid credentials. You have 2 attempts left."));
    }

    @Test
    void recordFailedLoginAttempt_existingUser_reachesMaxAttempts_shouldBlockAndThrowException() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setLoginAttempts(3);
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));
        ArgumentCaptor<LoginAttempts> attemptsCaptor = ArgumentCaptor.forClass(LoginAttempts.class);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> loginAttemptsService.recordFailedLoginAttempt(user));

        verify(loginAttemptsRepository).save(attemptsCaptor.capture());
        LoginAttempts savedAttempts = attemptsCaptor.getValue();
        assertEquals(4, savedAttempts.getLoginAttempts());
        assertTrue(savedAttempts.isBlocked());
        assertNotNull(savedAttempts.getBlockedUntil());
        assertTrue(exception.getMessage().contains("Your account has been locked for 5 days due to 4 failed login attempts."));
    }

    @Test
    void recordFailedLoginAttempt_blockedUserWithExpiredBlock_shouldResetBlockAndRecordAttempt() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setLoginAttempts(0);
        attempts.setBlocked(true);
        attempts.setBlockedUntil(LocalDateTime.now().minusDays(1));

        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));
        ArgumentCaptor<LoginAttempts> attemptsCaptor = ArgumentCaptor.forClass(LoginAttempts.class);

        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> loginAttemptsService.recordFailedLoginAttempt(user));

        verify(loginAttemptsRepository).save(attemptsCaptor.capture());
        LoginAttempts savedAttempts = attemptsCaptor.getValue();

        assertEquals(1, savedAttempts.getLoginAttempts());
        assertFalse(savedAttempts.isBlocked(), "Block should be reset as it was expired");
        assertNull(savedAttempts.getBlockedUntil(), "BlockedUntil should be null after reset");
        assertTrue(exception.getMessage().contains("Invalid credentials. You have 3 attempts left."));
    }


    @Test
    void resetLoginAttempts_userNotFound_shouldDoNothing() {
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.empty());
        loginAttemptsService.resetLoginAttempts(user);
        verify(loginAttemptsRepository, never()).save(any(LoginAttempts.class));
    }

    @Test
    void resetLoginAttempts_userFoundWithAttempts_shouldResetAndSave() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setLoginAttempts(2);
        attempts.setBlocked(true);
        attempts.setBlockedUntil(LocalDateTime.now().plusDays(1));
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));
        ArgumentCaptor<LoginAttempts> attemptsCaptor = ArgumentCaptor.forClass(LoginAttempts.class);

        loginAttemptsService.resetLoginAttempts(user);

        verify(loginAttemptsRepository).save(attemptsCaptor.capture());
        LoginAttempts savedAttempts = attemptsCaptor.getValue();
        assertEquals(0, savedAttempts.getLoginAttempts());
        assertFalse(savedAttempts.isBlocked());
        assertNull(savedAttempts.getBlockedUntil());
    }

    @Test
    void resetLoginAttempts_userFoundWithNoAttemptsAndNotBlocked_shouldNotSave() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setLoginAttempts(0);
        attempts.setBlocked(false);
        attempts.setBlockedUntil(null);
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));

        loginAttemptsService.resetLoginAttempts(user);

        verify(loginAttemptsRepository, never()).save(any(LoginAttempts.class));
    }

    @Test
    void resetLoginAttempts_userFoundOnlyBlocked_shouldResetAndSave() {
        LoginAttempts attempts = new LoginAttempts();
        attempts.setUser(user);
        attempts.setLoginAttempts(0);
        attempts.setBlocked(true);
        attempts.setBlockedUntil(LocalDateTime.now().plusDays(1));
        when(loginAttemptsRepository.findByUser(user)).thenReturn(Optional.of(attempts));
        ArgumentCaptor<LoginAttempts> attemptsCaptor = ArgumentCaptor.forClass(LoginAttempts.class);

        loginAttemptsService.resetLoginAttempts(user);

        verify(loginAttemptsRepository).save(attemptsCaptor.capture());
        LoginAttempts savedAttempts = attemptsCaptor.getValue();
        assertEquals(0, savedAttempts.getLoginAttempts());
        assertFalse(savedAttempts.isBlocked());
        assertNull(savedAttempts.getBlockedUntil());
    }
}