package com.extractor.unraveldocs.documents.service.impl;

import com.extractor.unraveldocs.config.DocumentConfigProperties;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionUploadData;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.documents.dto.response.FileEntryData;
import com.extractor.unraveldocs.documents.enums.DocumentStatus;
import com.extractor.unraveldocs.documents.enums.DocumentUploadState;
import com.extractor.unraveldocs.documents.interfaces.DocumentUploadService;
import com.extractor.unraveldocs.documents.model.DocumentCollection;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.documents.utils.SanitizeLogging;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.utils.imageupload.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentUploadImpl implements DocumentUploadService {
    private final CloudinaryService cloudinaryService;
    private final DocumentCollectionRepository documentCollectionRepository;
    private final DocumentConfigProperties documentConfigProperties;
    private final SanitizeLogging s;

    @Override
    @Transactional
    @CacheEvict(value = "documentCollections", key = "#user.id")
    public DocumentCollectionResponse<DocumentCollectionUploadData> uploadDocuments(MultipartFile[] files, User user) {
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
                            .uploadStatus(DocumentUploadState.SUCCESS.toString());

                    FileEntry fileEntry = fileEntryBuilder.build();
                    processedFileEntries.add(fileEntry);

                    fileEntryDataBuilder.documentId(fileEntry.getDocumentId())
                            .fileUrl(fileUrl)
                            .status(DocumentUploadState.SUCCESS.toString());
                    successfulUploads++;
                } catch (Exception storageEx) {
                    log.error("Failed to upload validated file {} to storage: {}",
                            s.sanitizeLogging(originalFilename),
                            s.sanitizeLogging(storageEx.getMessage()),
                            storageEx);
                    fileEntryBuilder.uploadStatus(DocumentUploadState.FAILED_STORAGE_UPLOAD.toString())
                            .errorMessage("Storage upload failed: " + storageEx.getMessage());

                    FileEntry fileEntry = fileEntryBuilder.build();
                    processedFileEntries.add(fileEntry);

                    fileEntryDataBuilder.documentId(fileEntry.getDocumentId())
                            .status(DocumentUploadState.FAILED_STORAGE.toString());
                    storageFailures++;
                }
            } catch (BadRequestException | IllegalArgumentException validationEx) {
                log.warn("Validation failed for file {}: {}",
                        s.sanitizeLogging(originalFilename),
                        s.sanitizeLogging(validationEx.getMessage()));
                String tempDocumentId = java.util.UUID.randomUUID().toString();
                fileEntryDataBuilder.documentId(tempDocumentId)
                        .status(DocumentUploadState.FAILED_VALIDATION.toString());
                validationFailures++;
            }
            responseFileEntriesData.add(fileEntryDataBuilder.build());
        }

        String savedCollectionId = null;

        if (!processedFileEntries.isEmpty()) {
            DocumentCollection documentCollection = DocumentCollection.builder()
                    .user(user)
                    .files(processedFileEntries)
                    .uploadTimestamp(OffsetDateTime.now())
                    .build();

            boolean allProcessedSucceededInStorage = processedFileEntries.stream()
                    .allMatch(fe -> DocumentUploadState.SUCCESS.toString().equals(fe.getUploadStatus()));
            boolean anyProcessedSucceededInStorage = processedFileEntries.stream()
                    .anyMatch(fe -> DocumentUploadState.SUCCESS.toString().equals(fe.getUploadStatus()));

            if (allProcessedSucceededInStorage) {
                documentCollection.setCollectionStatus(DocumentStatus.COMPLETED);
            } else if (anyProcessedSucceededInStorage) {
                documentCollection.setCollectionStatus(DocumentStatus.PARTIALLY_COMPLETED);
            } else {
                documentCollection.setCollectionStatus(DocumentStatus.FAILED_UPLOAD);
            }

            DocumentCollection savedCollection = documentCollectionRepository.save(documentCollection);
            savedCollectionId = savedCollection.getId();
            log.info("Document collection {} created with {} processed files for user {}. Status: {}",
                    s.sanitizeLogging(savedCollectionId),
                    processedFileEntries.size(),
                    s.sanitizeLogging(user.getId()),
                    savedCollection.getCollectionStatus());
        } else {
            if (totalFiles > 0) {
                log.info("No document collection created as all {} files failed validation for user {}", totalFiles,
                        s.sanitizeLogging(user.getId()));
            }
        }

        DocumentStatus apiResponseOverallStatus;
        if (totalFiles > 0 && successfulUploads == totalFiles) {
            apiResponseOverallStatus = DocumentStatus.COMPLETED;
        } else if (successfulUploads > 0 || (storageFailures > 0 && !processedFileEntries.isEmpty())) {
            apiResponseOverallStatus = DocumentStatus.PARTIALLY_COMPLETED;
        } else {
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
                apiResponseMessage =
                        String.format("No documents were successfully uploaded. %d failed validation. %d failed storage. Check individual statuses.",
                        validationFailures, storageFailures);
            } else {
                apiResponseMessage = "No files provided for upload.";
            }
        }

        DocumentCollectionUploadData uploadData = DocumentCollectionUploadData.builder()
                .collectionId(savedCollectionId)
                .overallStatus(apiResponseOverallStatus)
                .files(responseFileEntriesData)
                .build();

        return DocumentCollectionResponse.<DocumentCollectionUploadData>builder()
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
}