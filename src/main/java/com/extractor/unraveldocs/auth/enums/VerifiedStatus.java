package com.extractor.unraveldocs.auth.enums;

import lombok.Getter;

@Getter
public enum VerifiedStatus {
    PENDING("pending"),
    VERIFIED("verified"),
    EXPIRED("expired"),
    FAILED("failed");

    private final String status;

    VerifiedStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
