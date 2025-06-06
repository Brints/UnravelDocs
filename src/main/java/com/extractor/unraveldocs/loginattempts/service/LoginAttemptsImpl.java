package com.extractor.unraveldocs.loginattempts.service;

import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.loginattempts.interfaces.LoginAttemptsService;
import com.extractor.unraveldocs.loginattempts.model.LoginAttempts;
import com.extractor.unraveldocs.loginattempts.repository.LoginAttemptsRepository;
import com.extractor.unraveldocs.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginAttemptsImpl implements LoginAttemptsService {
    private final LoginAttemptsRepository loginAttemptsRepository;
    private static final int MAX_LOGIN_ATTEMPTS = 4;
    private static final int LOCKOUT_DURATION_DAYS = 5;

    @Override
    public void checkIfUserBlocked(User user) {
        Optional<LoginAttempts> attemptsOpt = loginAttemptsRepository.findByUser(user);
        if (attemptsOpt.isPresent()) {
            LoginAttempts attempts = attemptsOpt.get();
            if (
                    attempts.isBlocked() &&
                    attempts.getBlockedUntil() != null &&
                    attempts.getBlockedUntil().isAfter(LocalDateTime.now())) {

                LocalDateTime now = LocalDateTime.now();
                LocalDateTime blockedUntilTime = attempts.getBlockedUntil();

                Duration remainingDuration = Duration.between(now, blockedUntilTime);
                long totalRemainingSeconds = remainingDuration.getSeconds();

                // Calculate remaining days, rounding up.
                // Since blockedUntilTime.isAfter(now), totalRemainingSeconds will be > 0.
                // Math.ceil will ensure that any fraction of a day counts as a full day for the message.
                long daysToDisplay = (long) Math.ceil((double) totalRemainingSeconds / (24.0 * 60.0 * 60.0));

                // Ensure daysToDisplay is at least 1 if there's any time remaining.
                if (daysToDisplay < 1 && totalRemainingSeconds > 0) {
                    daysToDisplay = 1;
                } else if (daysToDisplay < 1) { // Should not be reached if blockedUntilTime.isAfter(now)
                    daysToDisplay = 1; // Default to 1 day if somehow calculated as 0 or less
                }

                String dayWord = daysToDisplay == 1 ? "day" : "days";
                String displayMessage = """
                        Your account is temporarily locked due to multiple failed login attempts.
                        Please try again after %d %s.
                        """.formatted(daysToDisplay, dayWord);
                throw new ForbiddenException(displayMessage);
            }
        }
    }

    @Override
    public void recordFailedLoginAttempt(User user) {
        LoginAttempts attempts = loginAttemptsRepository.findByUser(user)
                .orElseGet(() -> {
                    LoginAttempts newAttempts = new LoginAttempts();
                    newAttempts.setUser(user);
                    // Entity defaults: loginAttempts = 0, isBlocked = false
                    return newAttempts;
                });

        attempts.setLoginAttempts(attempts.getLoginAttempts() + 1);

        if (attempts.getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            attempts.setBlocked(true);
            // Always set/reset lockout time from now when max attempts are reached
            attempts.setBlockedUntil(LocalDateTime.now().plusDays(LOCKOUT_DURATION_DAYS));
            loginAttemptsRepository.save(attempts);
            throw new ForbiddenException("Your account has been locked for " + LOCKOUT_DURATION_DAYS + " days due to " + MAX_LOGIN_ATTEMPTS + " failed login attempts.");
        } else {
            // If user was previously blocked but lockout expired, and they fail again (but not enough to re-lock),
            // ensure isBlocked is false.
            if (attempts.isBlocked() && attempts.getBlockedUntil() != null && attempts.getBlockedUntil().isBefore(LocalDateTime.now())) {
                attempts.setBlocked(false);
                attempts.setBlockedUntil(null);
            }
            loginAttemptsRepository.save(attempts);
            int attemptsRemaining = MAX_LOGIN_ATTEMPTS - attempts.getLoginAttempts();
            String attemptWord = attemptsRemaining == 1 ? "attempt" : "attempts";
            throw new ForbiddenException("Invalid credentials. You have " + attemptsRemaining + " " + attemptWord + " left.");
        }
    }

    @Override
    public void resetLoginAttempts(User user) {
        Optional<LoginAttempts> attemptsOpt = loginAttemptsRepository.findByUser(user);
        if (attemptsOpt.isPresent()) {
            LoginAttempts attempts = attemptsOpt.get();
            // Only update if there's a change to avoid unnecessary DB write
            if (attempts.getLoginAttempts() != 0 || attempts.isBlocked() || attempts.getBlockedUntil() != null) {
                attempts.setLoginAttempts(0);
                attempts.setBlocked(false);
                attempts.setBlockedUntil(null);
                loginAttemptsRepository.save(attempts);
            }
        }
        // If no record exists, it implies 0 attempts and not blocked, so no action needed.
    }
}
