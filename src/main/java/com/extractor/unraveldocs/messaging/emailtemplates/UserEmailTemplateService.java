package com.extractor.unraveldocs.messaging.emailtemplates;

import com.extractor.unraveldocs.messaging.emailservice.mailgun.service.MailgunEmailService;
import com.extractor.unraveldocs.messaging.thymleafservice.ThymleafEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEmailTemplateService {
    private final ThymleafEmailService thymleafEmailService;
    private final MailgunEmailService mailgunEmailService;

    public void sendPasswordResetToken(String email, String firstName, String lastName, String token, String expiration) {
        String baseUrl = "http://localhost:8080";
        String resetPath = "/api/v1/user/reset-password";
        String resetUrl = baseUrl + resetPath + "?token=" + token + "&email=" + email;
        String emailContent = thymleafEmailService
                .generateSendPasswordResetTokenContent(firstName, lastName, resetUrl,
                expiration);
        mailgunEmailService.sendHtmlEmail(email, "Password Reset Token", emailContent);
    }

    public void sendSuccessfulPasswordReset(String email, String firstName, String lastName) {
        String emailContent = thymleafEmailService
                .successfulPasswordResetContent(firstName, lastName);
        mailgunEmailService.sendHtmlEmail(email, "Password Reset Successful", emailContent);
    }

    public void sendSuccessfulPasswordChange(String email, String firstName, String lastName) {
        String emailContent = thymleafEmailService
                .successfulPasswordChangeContent(firstName, lastName);
        mailgunEmailService.sendHtmlEmail(email, "Password Change Successful", emailContent);
    }

    public void scheduleUserDeletion(String email) {
        String emailContent = "Your account is scheduled for deletion. If you did not request this, please contact support.";
        mailgunEmailService.sendSimpleEmail(email, "User Deletion Scheduled", emailContent);
    }
}
