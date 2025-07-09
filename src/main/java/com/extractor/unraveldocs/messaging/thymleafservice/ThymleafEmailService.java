package com.extractor.unraveldocs.messaging.thymleafservice;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ThymleafEmailService {
    private final TemplateEngine templateEngine;

    public String generateSendVerificationEmailContent(
            String firstName,
            String lastName,
            String verificationUrl,
            String expiration) {

        Context context = new Context();

        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("expiration", expiration);
        return templateEngine.process("emailVerificationToken", context);
    }

    public String generateSendPasswordResetTokenContent(
            String firstName,
            String lastName,
            String resetUrl,
            String expiration
    ) {
        Context context = new Context();

        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("expiration", expiration);
        return templateEngine.process("passwordResetToken", context);
    }

    public String successfulPasswordResetContent(
            String firstName,
            String lastName
    ) {
        Context context = new Context();

        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        return templateEngine.process("successfulPasswordReset", context);
    }

    public String successfulPasswordChangeContent(
            String firstName,
            String lastName
    ) {
        Context context = new Context();

        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        return templateEngine.process("changePassword", context);
    }

    public String generateUserDeletionContent(
            String firstName,
            String lastName,
            String loginUrl,
            OffsetDateTime deletionDate
    ) {
        Context context = new Context();

        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("loginUrl", loginUrl);
        context.setVariable("deletionDate", deletionDate);
        return templateEngine.process("scheduleDeletion", context);
    }

    public String generateAccountDeletedEmail() {
        Context context = new Context();

        return templateEngine.process("accountDeleted", context);
    }
}
