package com.extractor.unraveldocs.global.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse<T> {
    int statusCode;
    String status;
    String message;
    T data;
}
