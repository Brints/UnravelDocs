package com.extractor.unraveldocs.utils.userlib;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

public class DateHelper {
    public LocalDateTime setExpiryDate(String format, int setTime) {
        LocalDateTime currentTime = LocalDateTime.now();
        return switch (format.toLowerCase()) {
            case "minute" -> currentTime.plusMinutes(setTime);
            case "hour" -> currentTime.plusHours(setTime);
            case "days" -> currentTime.plusDays(setTime);
            default -> throw new BadRequestException("Invalid format. Use 'minute', 'hour', or 'days'.");
        };
    }

    public long getTimeLeftToExpiry(LocalDateTime expiryDate, String format) {
        LocalDateTime currentTime = LocalDateTime.now();
        long timeLeft;
        switch (format.toLowerCase()) {
            case "minute" -> timeLeft = Duration.between(currentTime, expiryDate).toMinutes();
            case "hour" -> timeLeft = Duration.between(currentTime, expiryDate).toHours();
            case "days" -> timeLeft = Duration.between(currentTime, expiryDate).toDays();
            default -> throw new BadRequestException("Invalid format. Use 'minute', 'hour', or 'days'.");
        }
        return timeLeft;
    }
}
