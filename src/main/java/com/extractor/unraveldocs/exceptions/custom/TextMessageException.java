package com.extractor.unraveldocs.exceptions.custom;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TextMessageException extends RuntimeException {
  private final HttpStatus status;

    public TextMessageException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public TextMessageException(String message, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.status = status;
    }
}
