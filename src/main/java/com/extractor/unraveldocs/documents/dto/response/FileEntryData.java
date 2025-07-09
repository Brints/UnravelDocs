package com.extractor.unraveldocs.documents.dto.response;

import lombok.*;

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