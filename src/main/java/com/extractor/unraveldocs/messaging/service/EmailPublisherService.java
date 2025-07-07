package com.extractor.unraveldocs.messaging.service;

import com.extractor.unraveldocs.config.EmailRabbitMQConfig;
import com.extractor.unraveldocs.messaging.dto.EmailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailPublisherService {
    private final RabbitTemplate rabbitTemplate;

    public void queueEmail(EmailMessage emailMessage) {
        try {
            rabbitTemplate.convertAndSend(
                    EmailRabbitMQConfig.EXCHANGE_NAME,
                    EmailRabbitMQConfig.ROUTING_KEY,
                    emailMessage
            );

            log.info("Queued email for recipient: {} with subject: {}", emailMessage.getTo(), emailMessage.getSubject());
        } catch (Exception e) {
            log.error("Failed to queue email for recipient: {}. Error: {}",
                    emailMessage.getTo(), e.getMessage(), e);
        }
    }
}
