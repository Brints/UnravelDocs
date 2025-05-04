package com.extractor.unraveldocs.utils.generatetoken;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.SecureRandom;

@Component
public class GenerateVerificationToken {
    private final SecureRandom random = new SecureRandom();

    public String generateVerificationToken() {
        byte[] token = new byte[32];
        random.nextBytes(token);
        return new BigInteger(1, token).toString(16);
    }

    public String generateOtp(int len) {
        if (len < 1 || len > 6) {
            throw new IllegalArgumentException("OTP length should be between 1 and 6");
        }

        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp).substring(0, len);
    }
}
