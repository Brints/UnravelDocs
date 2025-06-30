package com.extractor.unraveldocs.ocrprocessing.utils;

import com.extractor.unraveldocs.config.DocumentConfigProperties;
import com.extractor.unraveldocs.documents.enums.DocumentUploadState;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.utils.SanitizeLogging;
import com.extractor.unraveldocs.utils.imageupload.cloudinary.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Component
@RequiredArgsConstructor
public class FileStorageService {
    private final CloudinaryService cloudinaryService;
    private final DocumentConfigProperties documentConfigProperties;

    public static int getStorageFailures(
            List<FileEntry> processedFiles,
            int storageFailures,
            String originalFilename,
            Exception e, Logger log, SanitizeLogging s) {
        log.error("Failed to upload validated file {} to storage: {}",
                s.sanitizeLogging(originalFilename),
                s.sanitizeLogging(e.getMessage()), e);

        FileEntry fileEntry = FileEntry.builder()
                .originalFileName(originalFilename)
                .uploadStatus(DocumentUploadState.FAILED_STORAGE_UPLOAD.toString())
                .errorMessage("Storage upload failed: " + e.getMessage())
                .build();

        processedFiles.add(fileEntry);

        storageFailures++;
        return storageFailures;
    }

    /**
     * Handles the successful upload of a file to storage.
     *
     * @param file              The uploaded file.
     * @param originalFileName  The original filename of the uploaded file.
     * @return A FileEntry object containing details of the uploaded file.
     */
    public FileEntry handleSuccessfulFileUpload(
            MultipartFile file,
            String originalFileName
    ) {
        String fileUrl = cloudinaryService.uploadFile(
                file,
                documentConfigProperties.getStorageFolder(),
                originalFileName,
                CloudinaryService.getRESOURCE_TYPE_IMAGE()
        );
        String publicId = cloudinaryService.generateRandomPublicId(originalFileName);

        return FileEntry.builder()
                .originalFileName(originalFileName)
                .fileUrl(fileUrl)
                .storageId(publicId)
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .uploadStatus(DocumentUploadState.SUCCESS.toString())
                .build();
    }
}
