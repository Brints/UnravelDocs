package com.extractor.unraveldocs.ocrprocessing.interfaces;

import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.ocrprocessing.dto.response.FileResultData;

public interface GetOcrDataService {
    DocumentCollectionResponse<FileResultData> getOcrData(String collectionId, String documentId, String userId);
}
