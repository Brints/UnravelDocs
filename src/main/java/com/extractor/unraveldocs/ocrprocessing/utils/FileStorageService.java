package com.extractor.unraveldocs.ocrprocessing.utils;

import com.extractor.unraveldocs.config.DocumentConfigProperties;
import com.extractor.unraveldocs.documents.dto.response.FileEntryData;
import com.extractor.unraveldocs.documents.enums.DocumentUploadState;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.utils.SanitizeLogging;
import com.extractor.unraveldocs.utils.imageupload.cloudinary.CloudinaryService;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
public class FileStorageService {
    private final CloudinaryService cloudinaryService;
    private final DocumentConfigProperties documentConfigProperties;

    public FileStorageService(CloudinaryService cloudinaryService, DocumentConfigProperties documentConfigProperties) {
        this.cloudinaryService = cloudinaryService;
        this.documentConfigProperties = documentConfigProperties;
    }

    public static int getStorageFailures(
            List<FileEntry> processedFiles,
            int storageFailures,
            String originalFilename,
            FileEntry.FileEntryBuilder fileEntryBuilder,
            FileEntryData.FileEntryDataBuilder fileEntryDataBuilder,
            Exception e, Logger log, SanitizeLogging s) {
        log.error("Failed to upload validated file {} to storage: {}",
                s.sanitizeLogging(originalFilename),
                s.sanitizeLogging(e.getMessage()), e);
        fileEntryBuilder.uploadStatus(DocumentUploadState.FAILED_STORAGE_UPLOAD.toString())
                .errorMessage("Storage upload failed: " + e.getMessage());

        FileEntry fileEntry = fileEntryBuilder.build();
        processedFiles.add(fileEntry);

        fileEntryDataBuilder.documentId(fileEntry.getDocumentId())
                .status(DocumentUploadState.FAILED_STORAGE.toString());
        storageFailures++;
        return storageFailures;
    }

    /**
     * Uploads a file to cloud storage and builds a FileEntry object with the result.
     *
     * @param file The file to upload.
     * @param originalFilename The original name of the file.
     * @param fileEntryBuilder The builder for the FileEntry.
     * @return A completed FileEntry object.
     */
    public FileEntry handleSuccessfulFileUpload(MultipartFile file, String originalFilename,
                                              FileEntry.FileEntryBuilder fileEntryBuilder) {
        String fileUrl = cloudinaryService.uploadFile(
                file,
                documentConfigProperties.getStorageFolder(),
                originalFilename,
                CloudinaryService.getRESOURCE_TYPE_IMAGE()
        );
        String publicId = cloudinaryService.generateRandomPublicId(originalFilename);

        fileEntryBuilder
                .fileUrl(fileUrl)
                .storageId(publicId)
                .uploadStatus(DocumentUploadState.SUCCESS.toString());

        return fileEntryBuilder.build();
    }
}
