package com.extractor.unraveldocs.documents.interfaces;

import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.user.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentUploadService {
    DocumentCollectionResponse uploadDocuments(MultipartFile[] files, User user);
    DocumentCollectionResponse deleteDocument(String collectionId, String userId);
    DocumentCollectionResponse deleteFileFromCollection(String collectionId, String documentId, String userId);
}