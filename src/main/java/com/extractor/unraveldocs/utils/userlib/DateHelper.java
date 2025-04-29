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

    public String getTimeLeftToExpiry(LocalDateTime expiryDate, String format) {
        if (expiryDate == null) {
            throw new BadRequestException("Expiry date cannot be null");
        }

        LocalDateTime currentTime = LocalDateTime.now();
        Duration duration = Duration.between(currentTime, expiryDate);
        String output;

        if (format.equals("minutes") || format.equals("minute")) {
            output = duration.toMinutes() < 2 ? "1 minute" : duration.toMinutes() + " minutes";
        } else if (format.equals("hours") || format.equals("hour")) {
            output = duration.toHours() < 2 ? "1 hour" :duration.toHours() + " hours";
        } else if (format.equals("days") || format.equals("day")) {
            output = duration.toDays() < 2 ? "1 day" : duration.toDays() + " days";
        } else {
            throw new BadRequestException("Invalid format. Use 'minutes', 'hours', or 'days'.");
        }

        return output;
    }
}
