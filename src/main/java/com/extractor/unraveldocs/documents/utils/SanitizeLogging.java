package com.extractor.unraveldocs.documents.utils;

import org.springframework.stereotype.Component;

@Component
public class SanitizeLogging {
    public String sanitizeLogging(String input) {
        if (input == null) {
            return "null";
        }
        return input.replace('\n', '_').replace('\r', '_');
    }
}
