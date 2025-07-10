package com.extractor.unraveldocs.messaging.service;

import com.extractor.unraveldocs.config.EmailRabbitMQConfig;
import com.extractor.unraveldocs.messaging.dto.EmailMessage;
import com.extractor.unraveldocs.messaging.emailservice.mailgun.service.MailgunEmailService;
import com.extractor.unraveldocs.messaging.thymleafservice.ThymleafEmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailMessageListener {
    private final ThymleafEmailService thymleafEmailService;
    private final MailgunEmailService mailgunEmailService;

    @Value("${app.base.url}")
    private String baseUrl;

    @RabbitListener(queues = EmailRabbitMQConfig.QUEUE_NAME)
    public void receiveEmailMessage(EmailMessage message) {
        log.info("Received email request for template: {}", message.getTemplateName());

        try {
            String emailContent = generateEmailContent(message);
            mailgunEmailService.sendHtmlEmail(message.getTo(), message.getSubject(), emailContent);
            log.info("Email sent successfully to: {} with subject: {}", message.getTo(), message.getSubject());
        } catch (Exception e) {
            log.error("Failed to send email to: {}. Error: {}", message.getTo(), e.getMessage(), e);

            // dead-letter queue or retry logic can be implemented here
        }
    }

    private String generateEmailContent(EmailMessage message) {
        Map<String, Object> model = message.getTemplateModel();

        return switch (message.getTemplateName()) {
            case "passwordResetToken" -> {
                String email = (String) model.get("email");
                String token = (String) model.get("token");

                String resetUrl = baseUrl + "/api/v1/user/reset-password?token=" + token + "&email=" + email;

                String expiration = model.get("expiration") instanceof OffsetDateTime
                        ? ((OffsetDateTime) model.get("expiration"))
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
                        : (String) model.get("expiration");

                yield thymleafEmailService.generateSendPasswordResetTokenContent(
                        (String) model.get("firstName"),
                        (String) model.get("lastName"),
                        resetUrl,
                        expiration
                );
            }
            case "successfulPasswordReset" -> thymleafEmailService.successfulPasswordResetContent(
                    (String) model.get("firstName"),
                    (String) model.get("lastName")
            );
            case "successfulPasswordChange" -> thymleafEmailService.successfulPasswordChangeContent(
                    (String) model.get("firstName"),
                    (String) model.get("lastName")
            );
            case "scheduleDeletion" -> {
                String loginUrl = baseUrl + "/api/v1/user/login";

                yield thymleafEmailService.generateUserDeletionContent(
                    (String) model.get("firstName"),
                    (String) model.get("lastName"),
                    loginUrl,
                    (OffsetDateTime) model.get("expiration")
                );
            }
            case "accountDeleted" -> thymleafEmailService.generateAccountDeletedEmail();
            case "emailVerificationToken" -> {
                String email = (String) model.get("email");
                String token = (String) model.get("token");

                String verificationUrl = baseUrl + "/api/v1/auth/verify-email?email=" + email + "&token=" + token;

                String expiration = model.get("expiration") instanceof OffsetDateTime
                        ? ((OffsetDateTime) model.get("expiration"))
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT))
                        : (String) model.get("expiration");


                yield thymleafEmailService.generateSendVerificationEmailContent(
                        (String) model.get("firstName"),
                        (String) model.get("lastName"),
                        verificationUrl,
                        expiration
                );
            }
            default -> throw new IllegalArgumentException("Unknown email template: " + message.getTemplateName());
        };
    }
}
