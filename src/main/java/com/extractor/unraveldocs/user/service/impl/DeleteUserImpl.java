package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.auth.repository.UserVerificationRepository;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.UserEmailTemplateService;
import com.extractor.unraveldocs.user.interfaces.userimpl.DeleteUserService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.utils.imageupload.aws.AwsS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserImpl implements DeleteUserService {
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final AwsS3Service awsS3Service;
    private final UserEmailTemplateService userEmailTemplateService;

    @Override
    @Transactional
    public void scheduleUserDeletion(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        LocalDateTime deletionDate = LocalDateTime.now().plusDays(10);
        user.setDeletedAt(deletionDate);

        if (user.getUserVerification() != null) {
            user.getUserVerification().setDeletedAt(deletionDate);
        }

        // TODO: Send email notification to the user
        userEmailTemplateService.scheduleUserDeletion(user.getEmail());

        userRepository.save(user);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkAndScheduleInactiveUsers() {
        log.info("Checking for inactive users to schedule deletion...");
        LocalDateTime threshold = LocalDateTime.now().minusMonths(12);

        List<User> inactiveUsers = userRepository.findAllByLastLoginDateBefore(threshold);

        for (User user : inactiveUsers) {
            LocalDateTime deletionDate = LocalDateTime.now().plusDays(10);
            user.setActive(false);
            user.setDeletedAt(deletionDate);

            if (user.getUserVerification() != null) {
                user.getUserVerification().setDeletedAt(deletionDate);
            }

            // TODO: Send email notification to the user
            userEmailTemplateService.scheduleUserDeletion(user.getEmail());
        }

        userRepository.saveAll(inactiveUsers);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 1 * * ?")
    @CacheEvict(
            value = {"getAllUsers", "getProfileByAdmin", "getProfileByUser"},
            allEntries = true
    )
    public void processScheduledDeletions() {
        log.info("Processing scheduled deletions...");
        LocalDateTime threshold = LocalDateTime.now();

        List<User> usersToDelete = userRepository.findAllByDeletedAtBefore(threshold);

        for (User user : usersToDelete) {
            // Delete user's profile picture from S3
            if (user.getProfilePicture() != null) {
                awsS3Service.deleteFile(user.getProfilePicture());
            }

            // Optionally, delete user verification data if applicable
            if (user.getUserVerification() != null) {
                userVerificationRepository.delete(user.getUserVerification());
            }
        }

        // Delete users from the repository
        userRepository.deleteAll(usersToDelete);
    }

    @Override
    @Transactional
    @CacheEvict(
            value = {"getAllUsers", "getProfileByAdmin", "getProfileByUser"},
            allEntries = true
    )
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getProfilePicture() != null) {
            awsS3Service.deleteFile(user.getProfilePicture());
        }

        if (user.getUserVerification() != null) {
            userVerificationRepository.delete(user.getUserVerification());
        }

        userRepository.delete(user);

        // TODO: Send email notification to the user
    }
}
