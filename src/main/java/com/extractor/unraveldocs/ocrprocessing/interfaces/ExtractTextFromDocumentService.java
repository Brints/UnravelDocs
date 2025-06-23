package com.extractor.unraveldocs.ocrprocessing.interfaces;

import com.extractor.unraveldocs.ocrprocessing.model.OcrData;

public interface ExtractTextFromDocumentService {
    OcrData extractTextFromDocument(String collectionId, String documentId, String userId);
}
