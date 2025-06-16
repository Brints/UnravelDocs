package com.extractor.unraveldocs.documents.service.impl;

import com.extractor.unraveldocs.config.DocumentConfigProperties;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionUploadData;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.documents.dto.response.FileEntryData;
import com.extractor.unraveldocs.documents.enums.DocumentStatus;
import com.extractor.unraveldocs.documents.interfaces.DocumentUploadService;
import com.extractor.unraveldocs.documents.model.DocumentCollection;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.utils.imageupload.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentUploadImpl implements DocumentUploadService {
    private final DocumentCollectionRepository documentCollectionRepository;
    private final CloudinaryService cloudinaryService;
    private final DocumentConfigProperties documentConfigProperties;

    @Override
    @Transactional
    public DocumentCollectionResponse uploadDocuments(MultipartFile[] files, User user) {
        List<FileEntry> processedFileEntries = new ArrayList<>();
        List<FileEntryData> responseFileEntriesData = new ArrayList<>();

        int totalFiles = files.length;
        int successfulUploads = 0;
        int validationFailures = 0;
        int storageFailures = 0;

        for (MultipartFile file : files) {
            String originalFilename = Objects.requireNonNullElse(file.getOriginalFilename(), "unnamed_file");
            FileEntry.FileEntryBuilder fileEntryBuilder = FileEntry.builder()
                    .originalFileName(originalFilename)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize());

            FileEntryData.FileEntryDataBuilder fileEntryDataBuilder = FileEntryData.builder()
                    .originalFileName(originalFilename)
                    .fileSize(file.getSize());

            try {
                validateFile(file);

                try {
                    String fileUrl = cloudinaryService.uploadFile(
                            file,
                            documentConfigProperties.getStorageFolder(),
                            originalFilename,
                            CloudinaryService.getRESOURCE_TYPE_IMAGE()
                    );
                    String publicId = cloudinaryService.generateRandomPublicId(originalFilename);

                    fileEntryBuilder.fileUrl(fileUrl)
                            .storageId(publicId)
                            .uploadStatus("SUCCESS");

                    FileEntry fileEntry = fileEntryBuilder.build();
                    processedFileEntries.add(fileEntry);

                    fileEntryDataBuilder.documentId(fileEntry.getDocumentId())
                            .fileUrl(fileUrl)
                            .status("SUCCESS")
                            .message("Uploaded successfully.");
                    successfulUploads++;
                } catch (Exception storageEx) {
                    log.error("Failed to upload validated file {} to storage: {}", originalFilename, storageEx.getMessage(), storageEx);
                    fileEntryBuilder.uploadStatus("FAILED_STORAGE_UPLOAD")
                            .errorMessage("Storage upload failed: " + storageEx.getMessage());

                    FileEntry fileEntry = fileEntryBuilder.build();
                    processedFileEntries.add(fileEntry);

                    fileEntryDataBuilder.documentId(fileEntry.getDocumentId())
                            .status("FAILED_STORAGE")
                            .message("Storage upload error: " + storageEx.getMessage());
                    storageFailures++;
                }
            } catch (BadRequestException | IllegalArgumentException validationEx) {
                log.warn("Validation failed for file {}: {}", originalFilename, validationEx.getMessage());
                String tempDocumentId = java.util.UUID.randomUUID().toString();
                fileEntryDataBuilder.documentId(tempDocumentId)
                        .status("FAILED_VALIDATION")
                        .message("Validation error: " + validationEx.getMessage());
                validationFailures++;
            }
            responseFileEntriesData.add(fileEntryDataBuilder.build());
        }

        String savedCollectionId = null;
        // DocumentStatus persistedCollectionEntityStatus = null; // Status of the actual DB entity

        if (!processedFileEntries.isEmpty()) {
            DocumentCollection documentCollection = DocumentCollection.builder()
                    .user(user)
                    .files(processedFileEntries)
                    .uploadTimestamp(OffsetDateTime.now())
                    .build();

            boolean allProcessedSucceededInStorage = processedFileEntries.stream()
                    .allMatch(fe -> "SUCCESS".equals(fe.getUploadStatus()));
            boolean anyProcessedSucceededInStorage = processedFileEntries.stream()
                    .anyMatch(fe -> "SUCCESS".equals(fe.getUploadStatus()));

            if (allProcessedSucceededInStorage) {
                documentCollection.setCollectionStatus(DocumentStatus.COMPLETED);
            } else if (anyProcessedSucceededInStorage) {
                documentCollection.setCollectionStatus(DocumentStatus.PARTIALLY_COMPLETED);
            } else {
                documentCollection.setCollectionStatus(DocumentStatus.FAILED_UPLOAD);
            }

            DocumentCollection savedCollection = documentCollectionRepository.save(documentCollection);
            savedCollectionId = savedCollection.getId();
            // persistedCollectionEntityStatus = savedCollection.getCollectionStatus();
            log.info("Document collection {} created with {} processed files for user {}. Status: {}",
                    savedCollectionId, processedFileEntries.size(), user.getId(), savedCollection.getCollectionStatus());
        } else {
            if (totalFiles > 0) {
                log.info("No document collection created as all {} files failed validation for user {}", totalFiles, user.getId());
            }
        }

        // Determine the overall status for the API response based on all input files
        DocumentStatus apiResponseOverallStatus;
        if (totalFiles > 0 && successfulUploads == totalFiles) {
            apiResponseOverallStatus = DocumentStatus.COMPLETED;
        } else if (successfulUploads > 0 || (storageFailures > 0 && !processedFileEntries.isEmpty())) {
            // If there's any success, or if there were files processed that resulted in storage failures
            apiResponseOverallStatus = DocumentStatus.PARTIALLY_COMPLETED;
        } else { // All failed validation, or no files provided, or all processed files failed storage
            apiResponseOverallStatus = DocumentStatus.FAILED_UPLOAD;
        }

        String apiResponseStatusString;
        String apiResponseMessage;
        if (totalFiles > 0 && successfulUploads == totalFiles) {
            apiResponseStatusString = "success";
            apiResponseMessage = "All " + totalFiles + " document(s) uploaded successfully.";
        } else if (successfulUploads > 0) {
            apiResponseStatusString = "partial_success";
            apiResponseMessage = String.format("%d document(s) uploaded successfully. %d failed validation. %d failed storage. Check individual statuses.",
                    successfulUploads, validationFailures, storageFailures);
        } else {
            apiResponseStatusString = "failure";
            if (totalFiles > 0 && validationFailures == totalFiles) {
                apiResponseMessage = "All " + totalFiles + " document(s) failed validation. No documents were uploaded.";
            } else if (totalFiles > 0) {
                apiResponseMessage = String.format("No documents were successfully uploaded. %d failed validation. %d failed storage. Check individual statuses.",
                        validationFailures, storageFailures);
            } else { // totalFiles == 0
                apiResponseMessage = "No files provided for upload.";
            }
        }

        DocumentCollectionUploadData uploadData = DocumentCollectionUploadData.builder()
                .collectionId(savedCollectionId)
                .overallStatus(apiResponseOverallStatus) // Use the status reflecting all input files
                .files(responseFileEntriesData)
                .build();

        return DocumentCollectionResponse.builder()
                .statusCode(HttpStatus.OK.value())
                .status(apiResponseStatusString)
                .message(apiResponseMessage)
                .data(uploadData)
                .build();
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty: " + file.getOriginalFilename());
        }
        String contentType = file.getContentType();
        if (contentType == null || !documentConfigProperties.getAllowedFileTypes().contains(contentType.toLowerCase())) {
            throw new BadRequestException(
                    "Invalid file type: " + contentType + " for file " + file.getOriginalFilename() +
                            ". Allowed types are: " + String.join(", ", documentConfigProperties.getAllowedFileTypes())
            );
        }
    }

    @Override
    @Transactional
    public DocumentCollectionResponse deleteDocument(String collectionId, String userId) {
        DocumentCollection collection = documentCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("Document collection not found with ID: " + collectionId));

        if (!collection.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to delete this document collection.");
        }

        for (FileEntry fileEntry : new ArrayList<>(collection.getFiles())) { // Iterate over a copy if modifying
            if ("SUCCESS".equals(fileEntry.getUploadStatus()) && fileEntry.getStorageId() != null) {
                try {
                    cloudinaryService.deleteFile(fileEntry.getStorageId());
                    log.info("Deleted file with storage ID {} (document ID {}) from Cloudinary for collection {}",
                            fileEntry.getStorageId(), fileEntry.getDocumentId(), collectionId);
                } catch (Exception e) {
                    log.error("Failed to delete file with storage ID {} (document ID {}) from Cloudinary for collection {}: {}",
                            fileEntry.getStorageId(), fileEntry.getDocumentId(), collectionId, e.getMessage());
                    // Potentially mark file as not deleted from storage or handle error
                }
            }
        }
        documentCollectionRepository.delete(collection);
        log.info("Document collection {} deleted successfully.", collectionId);

        return DocumentCollectionResponse.builder()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .status("success")
                .message("Document collection deleted successfully.")
                .data(null)
                .build();
    }

    @Override
    @Transactional
    public DocumentCollectionResponse deleteFileFromCollection(String collectionId, String documentId, String userId) {
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
                        entryToRemove.getStorageId(), entryToRemove.getDocumentId());
            } catch (Exception e) {
                log.error("Failed to delete file with storage ID {} (document ID {}) from Cloudinary. It will still be removed from the database. Error: {}",
                        entryToRemove.getStorageId(), entryToRemove.getDocumentId(), e.getMessage());
            }
        }

        boolean removed = collection.getFiles().remove(entryToRemove);
        if (!removed) {
            // This case should ideally not happen if fileToRemoveOpt was present and lists are handled correctly
            log.warn("FileEntry with document ID {} was found but not removed from the collection's list. Collection ID: {}", documentId, collectionId);
            // Consider if an exception should be thrown or how to handle this inconsistency
        }


        if (collection.getFiles().isEmpty()) {
            documentCollectionRepository.delete(collection);
            log.info("Document collection {} was empty after file (document ID {}) deletion and has been removed.", collectionId, documentId);
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
                    documentId, collectionId, collection.getCollectionStatus(), collection.getFiles().size());
        }

        return DocumentCollectionResponse.builder()
                .statusCode(HttpStatus.NO_CONTENT.value())
                .status("success")
                .message("File with document ID " + documentId + " deleted successfully from collection " + collectionId)
                .data(null)
                .build();
    }
}