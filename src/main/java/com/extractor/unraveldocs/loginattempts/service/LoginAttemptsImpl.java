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
    private static final int MAX_LOGIN_ATTEMPTS = 4;
    private static final int LOCKOUT_DURATION_DAYS = 5;
    private final LoginAttemptsRepository loginAttemptsRepository;

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
                String displayMessage = getDisplayMessage(remainingDuration);
                throw new ForbiddenException(displayMessage);
            }
        }
    }

    private static String getDisplayMessage(Duration remainingDuration) {
        long totalRemainingSeconds = remainingDuration.getSeconds();

        if (totalRemainingSeconds <= 0) {
            return "Your account is temporarily locked. Try again in 1 minute.";
        }

        long days = remainingDuration.toDays();
        if (days >= 1) {
            String dayWord = days == 1 ? "day" : "days";
            return "Your account is temporarily locked. Try again in %d %s.".formatted(days, dayWord);
        }

        long hours = remainingDuration.toHours();
        if (hours >= 1) {
            String hourWord = hours == 1 ? "hour" : "hours";
            return "Your account is temporarily locked. Try again in %d %s.".formatted(hours, hourWord);
        }

        long minutes = remainingDuration.toMinutes();
        if (minutes < 1) {
            minutes = 1;
        }
        String minuteWord = minutes == 1 ? "minute" : "minutes";
        return "Your account is temporarily locked. Try again in %d %s.".formatted(minutes, minuteWord);
    }

    @Override
    public void recordFailedLoginAttempt(User user) {
        LoginAttempts attempts = loginAttemptsRepository.findByUser(user)
                .orElseGet(() -> {
                    LoginAttempts newAttempts = new LoginAttempts();
                    newAttempts.setUser(user);
                    return newAttempts;
                });

        // Check if block period has expired and reset if needed
        if (attempts.isBlocked() &&
                attempts.getBlockedUntil() != null &&
                attempts.getBlockedUntil().isBefore(LocalDateTime.now())) {
            attempts.setLoginAttempts(0);
            attempts.setBlocked(false);
            attempts.setBlockedUntil(null);
        }

        // Now increment the attempt count
        attempts.setLoginAttempts(attempts.getLoginAttempts() + 1);

        if (attempts.getLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
            attempts.setBlocked(true);
            attempts.setBlockedUntil(LocalDateTime.now().plusDays(LOCKOUT_DURATION_DAYS));
            loginAttemptsRepository.save(attempts);
            throw new ForbiddenException("Your account has been locked. " + "Try again in " + LOCKOUT_DURATION_DAYS + " days.");
        } else {
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
            if (attempts.getLoginAttempts() != 0 || attempts.isBlocked() || attempts.getBlockedUntil() != null) {
                attempts.setLoginAttempts(0);
                attempts.setBlocked(false);
                attempts.setBlockedUntil(null);
                loginAttemptsRepository.save(attempts);
            }
        }
    }
}
