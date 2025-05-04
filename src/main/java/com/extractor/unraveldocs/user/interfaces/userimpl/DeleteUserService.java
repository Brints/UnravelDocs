package com.extractor.unraveldocs.user.interfaces.userimpl;

public interface DeleteUserService {
    void scheduleUserDeletion(String userId);
    void checkAndScheduleInactiveUsers();
    void processScheduledDeletions();
    void deleteUser(String userId);
}
