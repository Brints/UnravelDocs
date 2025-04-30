package com.extractor.unraveldocs.utils.templates;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

@Service
public class ThymleafEmailService {

    private final TemplateEngine templateEngine;

    public ThymleafEmailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String generateEmailContent(String fullName, String verificationUrl, String expiration) {
        Context context = new Context();
        context.setVariable("fullName", fullName);
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("expiration", expiration);
        return templateEngine.process("email-verification", context);
    }
}
