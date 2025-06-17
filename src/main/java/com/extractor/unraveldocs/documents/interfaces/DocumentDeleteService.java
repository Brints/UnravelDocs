package com.extractor.unraveldocs.documents.interfaces;

import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;

public interface DocumentDeleteService {
    void deleteDocument(String collectionId, String userId);
    void deleteFileFromCollection(String collectionId, String documentId, String userId);
}
