package com.extractor.unraveldocs.messaging.emailtemplates;

import com.extractor.unraveldocs.messaging.emailservice.mailgun.MailgunEmailService;
import com.extractor.unraveldocs.utils.templates.ThymleafEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthEmailTemplateService {
    private final ThymleafEmailService thymleafEmailService;
    private final MailgunEmailService mailgunEmailService;

    public void sendVerificationEmail(String email, String fullName, String token, String expiration) {

        String baseUrl = "http://localhost:8080";
        String verificationPath = "/api/v1/auth/verify-email";
        String verificationUrl = baseUrl + verificationPath + "?email=" + email + "&token=" + token;
        String emailContent = thymleafEmailService.generateEmailContent(fullName, verificationUrl, expiration);
        mailgunEmailService.sendHtmlEmail(email, "Email Verification Token", emailContent);
    }
}
