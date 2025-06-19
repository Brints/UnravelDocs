package com.extractor.unraveldocs.documents.service.impl;

import com.extractor.unraveldocs.documents.enums.DocumentStatus;
import com.extractor.unraveldocs.documents.enums.DocumentUploadState;
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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
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
    @Caching(
            evict = {
                    @CacheEvict(value = "documentCollection", key = "#collectionId"),
                    @CacheEvict(value = "documentCollections", key = "#userId")
            }
    )
    public void deleteDocument(String collectionId, String userId) {
        DocumentCollection collection = documentCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("Document collection not found with ID: " + collectionId));

        if (!collection.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to delete this document collection.");
        }

        for (FileEntry fileEntry : new ArrayList<>(collection.getFiles())) {
            if ("SUCCESS".equals(fileEntry.getUploadStatus()) && fileEntry.getStorageId() != null) {
                try {
                    cloudinaryService.deleteFile(fileEntry.getFileUrl());
                    log.info("Deleted file with storage ID {} (document ID {}) from Cloudinary for collection {}",
                            s.sanitizeLogging(fileEntry.getStorageId()),
                            s.sanitizeLogging(fileEntry.getDocumentId()),
                            s.sanitizeLogging(collectionId));
                } catch (Exception e) {
                    log.error("Failed to delete file with storage ID {} (document ID {}) from Cloudinary for collection {}: {}",
                            s.sanitizeLogging(fileEntry.getStorageId()),
                            s.sanitizeLogging(fileEntry.getDocumentId()),
                            s.sanitizeLogging(collectionId), s.sanitizeLogging(e.getMessage()));
                }
            }
        }
        documentCollectionRepository.delete(collection);
        log.info("Document collection {} deleted successfully.", s.sanitizeLogging(collectionId));
    }

    @Override
    @Transactional
    @Caching(
            evict = {
                    @CacheEvict(value = "documentCollection", key = "#collectionId"),
                    @CacheEvict(value = "documentCollections", key = "#userId"),
                    @CacheEvict(value = "fileEntry", key = "#collectionId + '-' + #documentId")
            }
    )
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

        if (
                DocumentUploadState.SUCCESS.toString().equals(entryToRemove.getUploadStatus()) &&
                entryToRemove.getStorageId() != null) {
            try {
                cloudinaryService.deleteFile(entryToRemove.getFileUrl());
                log.info("Successfully deleted file with storage ID {} (document ID {}) from Cloudinary.",
                        s.sanitizeLogging(entryToRemove.getStorageId()), s.sanitizeLogging(entryToRemove.getDocumentId()));
            } catch (Exception e) {
                log.error("Failed to delete file with storage ID {} (document ID {}) from Cloudinary. It will still be removed from the database. Error: {}",
                        s.sanitizeLogging(entryToRemove.getStorageId()),
                        s.sanitizeLogging(entryToRemove.getDocumentId()),
                        s.sanitizeLogging(e.getMessage()));
            }
        }

        boolean removed = collection.getFiles().remove(entryToRemove);
        if (!removed) {
            log.warn("FileEntry with document ID {} was found but not removed from the collection's list. Collection " +
                    "ID: {}", s.sanitizeLogging(documentId), s.sanitizeLogging(collectionId));
        }


        if (collection.getFiles().isEmpty()) {
            documentCollectionRepository.delete(collection);
            log.info("Document collection {} was empty after file (document ID {}) deletion and has been removed.",
                    s.sanitizeLogging(collectionId), s.sanitizeLogging(documentId));
        } else {
            boolean allRemainingSucceeded = collection.getFiles().stream()
                    .allMatch(fe -> DocumentUploadState.SUCCESS.toString().equals(fe.getUploadStatus()));
            boolean anyRemainingSucceeded = collection.getFiles().stream()
                    .anyMatch(fe -> DocumentUploadState.SUCCESS.toString().equals(fe.getUploadStatus()));

            if (allRemainingSucceeded) {
                collection.setCollectionStatus(DocumentStatus.COMPLETED);
            } else if (anyRemainingSucceeded) {
                collection.setCollectionStatus(DocumentStatus.PARTIALLY_COMPLETED);
            } else {
                collection.setCollectionStatus(DocumentStatus.FAILED_UPLOAD);
            }
            documentCollectionRepository.save(collection);
            log.info("File with document ID {} removed from collection {}. Collection updated. New status: {}. Remaining files: {}",
                    s.sanitizeLogging(documentId),
                    s.sanitizeLogging(collectionId),
                    collection.getCollectionStatus(),
                    collection.getFiles().size());
        }
    }
}
