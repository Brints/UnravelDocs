package com.extractor.unraveldocs.documents.interfaces;

import com.extractor.unraveldocs.documents.dto.response.*;

import java.util.List;

public interface GetDocumentService {
    DocumentCollectionResponse<GetDocumentCollectionData> getDocumentCollectionById(String collectionId, String userId);
    DocumentCollectionResponse<List<DocumentCollectionSummary>> getAllDocumentCollectionsByUser(String userId);
    DocumentCollectionResponse<FileEntryData> getFileFromCollection(String collectionId, String documentId, String userId);
}
