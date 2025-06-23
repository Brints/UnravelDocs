package com.extractor.unraveldocs.ocrprocessing.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OcrRequestMessage {
    private String collectionId;
    private String documentId;
}
