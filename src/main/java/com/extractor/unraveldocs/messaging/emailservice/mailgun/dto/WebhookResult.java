package com.extractor.unraveldocs.messaging.emailservice.mailgun.dto;

public record WebhookResult(
        String recipient,
        String event
) {
}
