package com.extractor.unraveldocs.documents.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileEntryData {
    private String documentId;
    private String originalFileName;
    private Long fileSize;
    private String fileUrl;
    private String status;
}