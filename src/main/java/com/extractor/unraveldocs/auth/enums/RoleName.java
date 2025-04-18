package com.extractor.unraveldocs.auth.enums;

public enum RoleName {
    USER("user"),
    ADMIN("admin");

    private final String role;

    RoleName(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return role;
    }
}
