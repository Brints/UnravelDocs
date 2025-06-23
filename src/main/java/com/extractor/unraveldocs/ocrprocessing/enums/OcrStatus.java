package com.extractor.unraveldocs.ocrprocessing.enums;

import lombok.Getter;

@Getter
public enum OcrStatus {
    PENDING("pending"),
    PROCESSING("processing"),
    COMPLETED("completed"),
    FAILED("failed");

    private final String status;

    OcrStatus(String status) {
        this.status = status;
    }

}
