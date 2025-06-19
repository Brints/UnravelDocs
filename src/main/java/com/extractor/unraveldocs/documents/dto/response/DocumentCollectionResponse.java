package com.extractor.unraveldocs.documents.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class DocumentCollectionResponse<T> {
    private int statusCode;
    private String status;
    private String message;
    private T data;
}
