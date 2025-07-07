package com.extractor.unraveldocs.messaging.emailtemplates;

import com.extractor.unraveldocs.messaging.dto.EmailMessage;
import com.extractor.unraveldocs.messaging.service.EmailPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserEmailTemplateService {
    private final EmailPublisherService emailPublisherService;

    @Value("${app.base.url}")
    private String baseUrl;

    public void sendPasswordResetToken(String email, String firstName, String lastName, String token, String expiration) {
        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject("Password Reset Token")
                .templateName("passwordResetToken")
                .templateModel(Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "resetUrl", baseUrl + "/api/v1/user/reset-password?token=" + token + "&email=" + email,
                        "expiration", expiration
                ))
                .build();

        emailPublisherService.queueEmail(message);
    }

    public void sendSuccessfulPasswordReset(String email, String firstName, String lastName) {
        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject("Password Reset Successful")
                .templateName("successfulPasswordReset")
                .templateModel(Map.of(
                        "firstName", firstName,
                        "lastName", lastName
                ))
                .build();

        emailPublisherService.queueEmail(message);
    }

    public void sendSuccessfulPasswordChange(String email, String firstName, String lastName) {
        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject("Password Change Successful")
                .templateName("successfulPasswordChange")
                .templateModel(Map.of(
                        "firstName", firstName,
                        "lastName", lastName
                ))
                .build();

        emailPublisherService.queueEmail(message);
    }

    public void scheduleUserDeletion(String email, String firstName, String lastName, OffsetDateTime deletionDate) {

        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject("Urgent! Your Account is Scheduled for Deletion")
                .templateName("scheduleDeletion")
                .templateModel(Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "deletionDate", deletionDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)),
                        "loginUrl", baseUrl + "/api/v1/auth/login"
                ))
                .build();

        emailPublisherService.queueEmail(message);
    }

    public void sendDeletedAccountEmail(String email) {
        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject("Your Account Has Been Deleted. \uD83D\uDE22")
                .templateName("accountDeleted")
                .templateModel(Map.of())
                .build();

        emailPublisherService.queueEmail(message);
    }
}
