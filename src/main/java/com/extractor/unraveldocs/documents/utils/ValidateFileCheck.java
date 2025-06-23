package com.extractor.unraveldocs.documents.utils;

import com.extractor.unraveldocs.config.DocumentConfigProperties;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class ValidateFileCheck {
    public static void validateFileCheck(MultipartFile file, DocumentConfigProperties documentConfigProperties) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty: " + file.getOriginalFilename());
        }
        String contentType = file.getContentType();
        if (contentType == null || !documentConfigProperties.getAllowedFileTypes().contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                    "Invalid file type: " + contentType +
                     " for file " + file.getOriginalFilename() +
                     ". Allowed types are: " +
                     String.join(", ", documentConfigProperties.getAllowedFileTypes())
            );
        }
    }
}
