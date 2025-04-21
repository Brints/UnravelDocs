package com.extractor.unraveldocs.auth.enums;

import lombok.Getter;

@Getter
public enum Role {
    USER("user"),
    MODERATOR("moderator"),
    ADMIN("admin");

    private final String role;

    Role(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }

    public static Role fromString(String role) {
        for (Role r : Role.values()) {
            if (r.getRole().equalsIgnoreCase(role)) {
                return r;
            }
        }
        throw new IllegalArgumentException("No enum constant " + Role.class.getCanonicalName() + "." + role);
    }
}
