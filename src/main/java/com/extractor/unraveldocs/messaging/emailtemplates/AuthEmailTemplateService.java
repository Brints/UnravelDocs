package com.extractor.unraveldocs.messaging.emailtemplates;

import com.extractor.unraveldocs.messaging.dto.EmailMessage;
import com.extractor.unraveldocs.messaging.service.EmailPublisherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthEmailTemplateService {
    private final EmailPublisherService emailPublisherService;

    public void sendVerificationEmail(String email, String firstName, String lastName, String token, String expiration) {
        EmailMessage message = EmailMessage.builder()
                .to(email)
                .subject("Email Verification Token")
                .templateName("emailVerificationToken")
                .templateModel(Map.of(
                        "firstName", firstName,
                        "lastName", lastName,
                        "email", email,
                        "token", token,
                        "expiration", expiration
                ))
                .build();
        emailPublisherService.queueEmail(message);
    }
}
