package com.extractor.unraveldocs.documents.service;

import com.extractor.unraveldocs.documents.dto.response.*;
import com.extractor.unraveldocs.documents.interfaces.ClearAllCollectionsService;
import com.extractor.unraveldocs.documents.interfaces.DocumentDeleteService;
import com.extractor.unraveldocs.documents.interfaces.DocumentUploadService;
import com.extractor.unraveldocs.documents.interfaces.GetDocumentService;
import com.extractor.unraveldocs.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentService {
    private final ClearAllCollectionsService clearAllCollectionsService;
    private final DocumentDeleteService documentDeleteService;
    private final DocumentUploadService documentUploadService;
    private final GetDocumentService getDocumentService;

    public DocumentCollectionResponse<DocumentCollectionUploadData> uploadDocuments(MultipartFile[] files, User user) {
        return documentUploadService.uploadDocuments(files, user);
    }

    public void deleteDocument(String collectionId, String userId) {
        documentDeleteService.deleteDocument(collectionId, userId);
    }

    public void deleteFileFromCollection(String collectionId, String documentId, String userId) {
        documentDeleteService.deleteFileFromCollection(collectionId, documentId, userId);
    }

    public DocumentCollectionResponse<GetDocumentCollectionData> getDocumentCollectionById(String collectionId, String userId) {
        return getDocumentService.getDocumentCollectionById(collectionId, userId);
    }

    public DocumentCollectionResponse<List<DocumentCollectionSummary>> getAllDocumentCollectionsByUser(String userId) {
        return getDocumentService.getAllDocumentCollectionsByUser(userId);
    }

    public DocumentCollectionResponse<FileEntryData> getFileFromCollection(String collectionId, String documentId, String userId) {
        return getDocumentService.getFileFromCollection(collectionId, documentId, userId);
    }

    public void clearAllCollections(String userId) {
        clearAllCollectionsService.clearAllCollections(userId);
    }
}