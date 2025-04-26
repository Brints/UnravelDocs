package com.extractor.unraveldocs.exceptions.custom;

import lombok.Getter;

@Getter
public class JwtAuthenticationException extends RuntimeException {
    private final String errorCode;

    public JwtAuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}
