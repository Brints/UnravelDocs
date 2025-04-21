package com.extractor.unraveldocs.auth.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class VerifiedStatusConverter implements AttributeConverter<VerifiedStatus, String> {

    @Override
    public String convertToDatabaseColumn(VerifiedStatus status) {
        if (status == null) {
            return null;
        }
        return status.toString();
    }

    @Override
    public VerifiedStatus convertToEntityAttribute(String status) {
        if (status == null) {
            return null;
        }

        for (VerifiedStatus s : VerifiedStatus.values()) {
            if (s.toString().equalsIgnoreCase(status)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown verified status: " + status);
    }
}
