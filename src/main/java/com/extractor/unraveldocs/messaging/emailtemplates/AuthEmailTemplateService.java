package com.extractor.unraveldocs.messaging.emailtemplates;

import com.extractor.unraveldocs.messaging.emailservice.mailgun.service.MailgunEmailService;
import com.extractor.unraveldocs.messaging.thymleafservice.ThymleafEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthEmailTemplateService {
    private final ThymleafEmailService thymleafEmailService;
    private final MailgunEmailService mailgunEmailService;

    @Value("${app.base.url}")
    private String baseUrl;

    @Async
    public void sendVerificationEmail(String email, String firstName, String lastName, String token, String expiration) {

        String verificationPath = "/api/v1/auth/verify-email";
        String verificationUrl = baseUrl + verificationPath + "?email=" + email + "&token=" + token;
        String emailContent = thymleafEmailService
                .generateSendVerificationEmailContent(firstName, lastName, verificationUrl,
                expiration);
        mailgunEmailService.sendHtmlEmail(email, "Email Verification Token", emailContent);
    }
}
