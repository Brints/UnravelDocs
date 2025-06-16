package com.extractor.unraveldocs.exceptions.custom;

public class FileTooLargeException extends RuntimeException {
    public FileTooLargeException(String message) {
        super(message);
    }
}
