package com.extractor.unraveldocs.global.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UnravelDocsDataResponse<T> {
    int statusCode;
    String status;
    String message;
    T data;
}
