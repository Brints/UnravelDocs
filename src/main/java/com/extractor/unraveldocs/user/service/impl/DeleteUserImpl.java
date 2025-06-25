package com.extractor.unraveldocs.user.service.impl;

import com.extractor.unraveldocs.auth.repository.UserVerificationRepository;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.messaging.emailtemplates.UserEmailTemplateService;
import com.extractor.unraveldocs.user.interfaces.userimpl.DeleteUserService;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import com.extractor.unraveldocs.utils.imageupload.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserImpl implements DeleteUserService {
    private final UserRepository userRepository;
    private final UserVerificationRepository userVerificationRepository;
    private final CloudinaryService cloudinaryService;
    private final UserEmailTemplateService userEmailTemplateService;

    @Override
    @Transactional
    public void scheduleUserDeletion(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        OffsetDateTime deletionDate = OffsetDateTime.now().plusDays(10);
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
        OffsetDateTime threshold = OffsetDateTime.now().minusMonths(12);

        List<User> inactiveUsers = userRepository.findAllByLastLoginDateBefore(threshold);

        for (User user : inactiveUsers) {
            OffsetDateTime deletionDate = OffsetDateTime.now().plusDays(10);
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
        OffsetDateTime threshold = OffsetDateTime.now();

        List<User> usersToDelete = userRepository.findAllByDeletedAtBefore(threshold);

        for (User user : usersToDelete) {
            if (user.getProfilePicture() != null) {
                cloudinaryService.deleteFile(user.getProfilePicture());
            }

            if (user.getUserVerification() != null) {
                userVerificationRepository.delete(user.getUserVerification());
            }
        }

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
            cloudinaryService.deleteFile(user.getProfilePicture());
        }

        if (user.getUserVerification() != null) {
            userVerificationRepository.delete(user.getUserVerification());
        }

        userRepository.delete(user);

        // TODO: Send email notification to the user
    }
}
