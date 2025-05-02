package com.extractor.unraveldocs.messaging.emailservice.mailgun.controller;

import com.extractor.unraveldocs.messaging.emailservice.mailgun.dto.WebhookResult;
//import com.extractor.unraveldocs.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class MailgunWebhookController {
    //private final UserService userService;

    @Operation(summary = "Handle Mailgun webhook events")
    @PostMapping("/mailgun")
    public ResponseEntity<Void> handleMailgunWebhook(@RequestBody WebhookResult payload) {
        // Process the webhook payload
        String recipient = payload.recipient();
        String event = payload.event();

        switch (event) {
            case "delivered":
                // Handle email delivery
                log.info("Email delivered to: {}", recipient);
                break;
            case "opened":
                // Handle email opened
                log.info("Email opened by: {}", recipient);
                break;
            case "clicked":
                // Handle email clicked
                log.info("Email clicked by: {}", recipient);
                break;
            case "failed":
                // Handle email failure
                log.error("Email delivery failed for: {}", recipient);
                break;
            default:
                // Handle unknown event
                log.warn("Unknown event type: {} for recipient: {}", event, recipient);
                break;
        }

        return ResponseEntity.ok().build();
    }

}
