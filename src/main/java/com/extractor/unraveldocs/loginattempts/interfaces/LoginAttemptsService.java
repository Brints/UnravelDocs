package com.extractor.unraveldocs.loginattempts.interfaces;

import com.extractor.unraveldocs.user.model.User;

public interface LoginAttemptsService {
    void checkIfUserBlocked(User user);
    void recordFailedLoginAttempt(User user);
    void resetLoginAttempts(User user);
}
