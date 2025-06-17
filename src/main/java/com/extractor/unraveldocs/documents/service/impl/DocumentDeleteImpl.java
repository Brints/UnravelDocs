package com.extractor.unraveldocs.documents.service.impl;

import com.extractor.unraveldocs.documents.enums.DocumentStatus;
import com.extractor.unraveldocs.documents.interfaces.DocumentDeleteService;
import com.extractor.unraveldocs.documents.model.DocumentCollection;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.documents.utils.SanitizeLogging;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.utils.imageupload.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentDeleteImpl implements DocumentDeleteService {
    private final CloudinaryService cloudinaryService;
    private final DocumentCollectionRepository documentCollectionRepository;
    private final SanitizeLogging s;

    @Override
    @Transactional
    public void deleteDocument(String collectionId, String userId) {
        DocumentCollection collection = documentCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("Document collection not found with ID: " + collectionId));

        if (!collection.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to delete this document collection.");
        }

        for (FileEntry fileEntry : new ArrayList<>(collection.getFiles())) {
            if ("SUCCESS".equals(fileEntry.getUploadStatus()) && fileEntry.getStorageId() != null) {
                try {
                    cloudinaryService.deleteFile(fileEntry.getStorageId());
                    log.info("Deleted file with storage ID {} (document ID {}) from Cloudinary for collection {}",
                            s.sanitizeLogging(fileEntry.getStorageId()), s.sanitizeLogging(fileEntry.getDocumentId()),
                            s.sanitizeLogging(collectionId));
                } catch (Exception e) {
                    log.error("Failed to delete file with storage ID {} (document ID {}) from Cloudinary for collection {}: {}",
                            s.sanitizeLogging(fileEntry.getStorageId()), s.sanitizeLogging(fileEntry.getDocumentId()),
                            s.sanitizeLogging(collectionId), s.sanitizeLogging(e.getMessage()));
                }
            }
        }
        documentCollectionRepository.delete(collection);
        log.info("Document collection {} deleted successfully.", s.sanitizeLogging(collectionId));
    }

    @Override
    @Transactional
    public void deleteFileFromCollection(String collectionId, String documentId, String userId) {
        DocumentCollection collection = documentCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("Document collection not found with ID: " + collectionId));

        if (!collection.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to modify this document collection.");
        }

        Optional<FileEntry> fileToRemoveOpt = collection.getFiles().stream()
                .filter(fileEntry -> documentId.equals(fileEntry.getDocumentId()))
                .findFirst();

        if (fileToRemoveOpt.isEmpty()) {
            throw new NotFoundException("File with document ID: " + documentId + " not found in collection: " + collectionId);
        }

        FileEntry entryToRemove = fileToRemoveOpt.get();

        if ("SUCCESS".equals(entryToRemove.getUploadStatus()) && entryToRemove.getStorageId() != null) {
            try {
                cloudinaryService.deleteFile(entryToRemove.getStorageId());
                log.info("Successfully deleted file with storage ID {} (document ID {}) from Cloudinary.",
                        s.sanitizeLogging(entryToRemove.getStorageId()), s.sanitizeLogging(entryToRemove.getDocumentId()));
            } catch (Exception e) {
                log.error("Failed to delete file with storage ID {} (document ID {}) from Cloudinary. It will still be removed from the database. Error: {}",
                        s.sanitizeLogging(entryToRemove.getStorageId()), s.sanitizeLogging(entryToRemove.getDocumentId()),
                    s.sanitizeLogging(e.getMessage()));
            }
        }

        boolean removed = collection.getFiles().remove(entryToRemove);
        if (!removed) {
            // This case should ideally not happen if fileToRemoveOpt was present and lists are handled correctly
            log.warn("FileEntry with document ID {} was found but not removed from the collection's list. Collection " +
                    "ID: {}", s.sanitizeLogging(documentId), s.sanitizeLogging(collectionId));
            // Consider if an exception should be thrown or how to handle this inconsistency
        }


        if (collection.getFiles().isEmpty()) {
            documentCollectionRepository.delete(collection);
            log.info("Document collection {} was empty after file (document ID {}) deletion and has been removed.",
                    s.sanitizeLogging(collectionId), s.sanitizeLogging(documentId));
        } else {
            // Recalculate collection status based on remaining files
            boolean allRemainingSucceeded = collection.getFiles().stream()
                    .allMatch(fe -> "SUCCESS".equals(fe.getUploadStatus()));
            boolean anyRemainingSucceeded = collection.getFiles().stream()
                    .anyMatch(fe -> "SUCCESS".equals(fe.getUploadStatus()));

            if (allRemainingSucceeded) {
                collection.setCollectionStatus(DocumentStatus.COMPLETED);
            } else if (anyRemainingSucceeded) {
                collection.setCollectionStatus(DocumentStatus.PARTIALLY_COMPLETED);
            } else { // All remaining files have a non-SUCCESS status (e.g., FAILED_STORAGE_UPLOAD)
                collection.setCollectionStatus(DocumentStatus.FAILED_UPLOAD);
            }
            documentCollectionRepository.save(collection);
            log.info("File with document ID {} removed from collection {}. Collection updated. New status: {}. Remaining files: {}",
                    s.sanitizeLogging(documentId), s.sanitizeLogging(collectionId), collection.getCollectionStatus(),
                collection.getFiles().size());
        }
    }
}
