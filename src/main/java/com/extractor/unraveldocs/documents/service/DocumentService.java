package com.extractor.unraveldocs.documents.service;

import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.documents.interfaces.DocumentDeleteService;
import com.extractor.unraveldocs.documents.interfaces.DocumentUploadService;
import com.extractor.unraveldocs.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentDeleteService documentDeleteService;
    private final DocumentUploadService documentUploadService;

    public DocumentCollectionResponse uploadDocuments(MultipartFile[] files, User user) {
        return documentUploadService.uploadDocuments(files, user);
    }

    public void deleteDocument(String collectionId, String userId) {
        documentDeleteService.deleteDocument(collectionId, userId);
    }

    public void deleteFileFromCollection(String collectionId, String fileStorageId, String userId) {
       documentDeleteService.deleteFileFromCollection(collectionId, fileStorageId, userId);
    }
}