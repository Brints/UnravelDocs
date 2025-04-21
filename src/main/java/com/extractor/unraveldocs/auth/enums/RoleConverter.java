package com.extractor.unraveldocs.auth.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }
        return role.getRole();
    }

    @Override
    public Role convertToEntityAttribute(String role) {
        if (role == null) {
            return null;
        }

        return Role.fromString(role);
    }
}
