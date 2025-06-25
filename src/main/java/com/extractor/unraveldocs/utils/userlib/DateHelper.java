package com.extractor.unraveldocs.utils.userlib;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;

@Slf4j
@Component
public class DateHelper {

    public OffsetDateTime setExpiryDate(OffsetDateTime now, String format, int setTime) {
        return switch (format.toLowerCase()) {
            case "minute" -> now.plusMinutes(setTime);
            case "hour" -> now.plusHours(setTime);
            case "days" -> now.plusDays(setTime);
            default -> throw new BadRequestException("Invalid format. Use 'minute', 'hour', or 'days'.");
        };
    }

    public String getTimeLeftToExpiry(OffsetDateTime now, OffsetDateTime expiryDate, String format) {
        if (expiryDate == null) {
            throw new BadRequestException("Expiry date cannot be null");
        }

        Duration duration = Duration.between(now, expiryDate);

        return switch (format.toLowerCase()) {
            case "minutes", "minute" -> duration.toMinutes() < 2 ? "1 minute" : duration.toMinutes() + " minutes";
            case "hours", "hour" -> duration.toHours() < 2 ? "1 hour" : duration.toHours() + " hours";
            case "days", "day" -> duration.toDays() < 2 ? "1 day" : duration.toDays() + " days";
            default -> throw new BadRequestException("Invalid format. Use 'minutes', 'hours', or 'days'.");
        };
    }
}
