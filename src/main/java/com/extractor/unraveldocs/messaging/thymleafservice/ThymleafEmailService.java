package com.extractor.unraveldocs.messaging.thymleafservice;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class ThymleafEmailService {

    private final TemplateEngine templateEngine;
    private final Context context = new Context();

    public ThymleafEmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generateSendVerificationEmailContent(
            String firstName,
            String lastName,
            String verificationUrl,
            String expiration) {
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("expiration", expiration);
        return templateEngine.process("email-verification", context);
    }

    public String generateSendPasswordResetTokenContent(
            String firstName,
            String lastName,
            String resetUrl,
            String expiration
    ) {
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("expiration", expiration);
        return templateEngine.process("password-reset-token", context);
    }

    public String successfulPasswordResetContent(
            String firstName,
            String lastName
    ) {
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        return templateEngine.process("successful-password-reset", context);
    }

    public String successfulPasswordChangeContent(
            String firstName,
            String lastName
    ) {
        context.setVariable("firstName", firstName);
        context.setVariable("lastName", lastName);
        return templateEngine.process("change-password", context);
    }
}
