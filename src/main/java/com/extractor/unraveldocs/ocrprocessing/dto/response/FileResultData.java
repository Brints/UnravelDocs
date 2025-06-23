package com.extractor.unraveldocs.ocrprocessing.dto.response;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class FileResultData {
    private String documentId;
    private String originalFileName;
    private String status;
    private String errorMessage;
    private OffsetDateTime createdAt;
    private String extractedText;
}
