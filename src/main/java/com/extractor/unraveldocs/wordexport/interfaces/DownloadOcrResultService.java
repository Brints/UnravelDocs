package com.extractor.unraveldocs.wordexport.interfaces;

import org.springframework.core.io.InputStreamResource;

public interface DownloadOcrResultService {
    record DownloadableFile(String fileName, InputStreamResource resource) {}

    DownloadableFile downloadAsDocx(String collectionId, String documentId, String userId);
}
