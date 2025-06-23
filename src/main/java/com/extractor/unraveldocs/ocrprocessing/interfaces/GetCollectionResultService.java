package com.extractor.unraveldocs.ocrprocessing.interfaces;

import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.ocrprocessing.dto.response.CollectionResultResponse;

public interface GetCollectionResultService {
    DocumentCollectionResponse<CollectionResultResponse> getCollectionResult(String collectionId);
}
