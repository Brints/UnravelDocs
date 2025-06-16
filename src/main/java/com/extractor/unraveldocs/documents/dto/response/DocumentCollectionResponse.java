package com.extractor.unraveldocs.documents.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DocumentCollectionResponse {
    private int statusCode;
    private String status;
    private String message;
    private DocumentCollectionUploadData data;
}
