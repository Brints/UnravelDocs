package com.extractor.unraveldocs.utils.userlib;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class DateHelper {
    private static final Logger log = LoggerFactory.getLogger(DateHelper.class);

    public LocalDateTime setExpiryDate(LocalDateTime now, String format, int setTime) {
        return switch (format.toLowerCase()) {
            case "minute" -> now.plusMinutes(setTime);
            case "hour" -> now.plusHours(setTime);
            case "days" -> now.plusDays(setTime);
            default -> throw new BadRequestException("Invalid format. Use 'minute', 'hour', or 'days'.");
        };
    }

    public String getTimeLeftToExpiry(LocalDateTime now, LocalDateTime expiryDate, String format) {
        if (expiryDate == null) {
            throw new BadRequestException("Expiry date cannot be null");
        }

        Duration duration = Duration.between(now, expiryDate);
        log.info("Duration in get time in hours {} ", duration);
        log.info("Get time in hours {} ", duration.toHours());

        return switch (format.toLowerCase()) {
            case "minutes", "minute" -> duration.toMinutes() < 2 ? "1 minute" : duration.toMinutes() + " minutes";
            case "hours", "hour" -> duration.toHours() < 2 ? "1 hour" : duration.toHours() + " hours";
            case "days", "day" -> duration.toDays() < 2 ? "1 day" : duration.toDays() + " days";
            default -> throw new BadRequestException("Invalid format. Use 'minutes', 'hours', or 'days'.");
        };
    }
}
