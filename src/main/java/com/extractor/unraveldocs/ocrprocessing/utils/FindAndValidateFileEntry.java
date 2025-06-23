package com.extractor.unraveldocs.ocrprocessing.utils;

import com.extractor.unraveldocs.documents.model.DocumentCollection;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import org.springframework.stereotype.Component;

@Component
public class FindAndValidateFileEntry {
    public FileEntry findAndValidateFileEntry(String collectionId, String documentId, String userId,
                                              DocumentCollectionRepository documentCollectionRepository) {
        DocumentCollection collection = documentCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("Document collection not found with ID: " + collectionId));

        if (!collection.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to access this document.");
        }

        return collection.getFiles().stream()
                .filter(f -> f.getDocumentId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Document not found with ID: " + documentId));
    }
}
