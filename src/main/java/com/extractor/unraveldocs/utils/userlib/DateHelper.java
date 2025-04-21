package com.extractor.unraveldocs.utils.userlib;

import com.extractor.unraveldocs.exceptions.custom.BadRequestException;

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
}
