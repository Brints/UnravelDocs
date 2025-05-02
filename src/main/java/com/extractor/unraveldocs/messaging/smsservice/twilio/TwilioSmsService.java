package com.extractor.unraveldocs.messaging.smsservice.twilio;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TwilioSmsService {
    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String phoneNumber;

    public void sendSms(String to, String message) {
        Twilio.init(accountSid, authToken);

        Message messageResponse = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(phoneNumber),
                message
        ).create();

        log.info("Message sent: {}", messageResponse.getSid());
    }
}
