package com.extractor.unraveldocs.utils.userlib;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
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

    public String getTimeLeftToExpiry(LocalDateTime expiryDate, String format) {
        if (expiryDate == null) {
            throw new BadRequestException("Expiry date cannot be null");
        }

        Duration duration = Duration.between(LocalDateTime.now(), expiryDate);

        return switch (format) {
            case "minutes", "minute" -> duration.toMinutes() < 2 ? "1 minute" : duration.toMinutes() + " minutes";
            case "hours", "hour" -> duration.toHours() < 2 ? "1 hour" : duration.toHours() + " hours";
            case "days", "day" -> duration.toDays() < 2 ? "1 day" : duration.toDays() + " days";
            default -> throw new BadRequestException("Invalid format. Use 'minutes', 'hours', or 'days'.");
        };
    }
}
