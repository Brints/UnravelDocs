package com.extractor.unraveldocs.messaging.smsservice.awssns;

import com.extractor.unraveldocs.exceptions.custom.TextMessageException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SnsException;

@Service
@RequiredArgsConstructor
public class AwsSnsService {
    private final SnsClient snsClient;

    public void sendSms(String phoneNumber, String message) {
        PublishRequest publishRequest = PublishRequest.builder()
                .message(message)
                .phoneNumber(phoneNumber)
                .build();

        try {
            snsClient.publish(publishRequest);
        } catch (SnsException e) {
            switch (e) {
                case InvalidParameterException invalidParameterException ->
                        throw new TextMessageException("Invalid parameter: " + e.getMessage(), HttpStatus.BAD_REQUEST);
                case InternalErrorException internalErrorException ->
                        throw new TextMessageException("Internal error: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
                case ThrottledException throttledException ->
                        throw new TextMessageException("Throttled: " + e.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
                default ->
                        throw new TextMessageException("Failed to send SMS: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
