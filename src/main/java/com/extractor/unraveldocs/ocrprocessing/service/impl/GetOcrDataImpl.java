package com.extractor.unraveldocs.ocrprocessing.service.impl;

import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.ocrprocessing.dto.response.FileResultData;
import com.extractor.unraveldocs.ocrprocessing.interfaces.GetOcrDataService;
import com.extractor.unraveldocs.ocrprocessing.model.OcrData;
import com.extractor.unraveldocs.ocrprocessing.repository.OcrDataRepository;
import com.extractor.unraveldocs.ocrprocessing.utils.FindAndValidateFileEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GetOcrDataImpl implements GetOcrDataService {
    private final DocumentCollectionRepository documentCollectionRepository;
    private final OcrDataRepository ocrDataRepository;
    private final FindAndValidateFileEntry validateFileEntry;

    @Override
    @Transactional(readOnly = true)
    public DocumentCollectionResponse<FileResultData> getOcrData(String collectionId, String documentId, String userId) {
        FileEntry fileEntry = validateFileEntry
                .findAndValidateFileEntry(collectionId, documentId, userId, documentCollectionRepository);

        Optional<OcrData> ocrDataOptional = ocrDataRepository.findByDocumentId(fileEntry.getDocumentId());
        FileResultData fileResultData = getFileResultData(ocrDataOptional, fileEntry);

        return DocumentCollectionResponse.<FileResultData>builder()
                .statusCode(200)
                .status("success")
                .message("OCR data retrieved successfully.")
                .data(fileResultData)
                .build();
    }

    private static FileResultData getFileResultData(Optional<OcrData> ocrDataOptional, FileEntry fileEntry) {
        if (ocrDataOptional.isEmpty()) {
            throw new NotFoundException("OCR data not found for document ID: " + fileEntry.getDocumentId());
        }

        OcrData ocrData = ocrDataOptional.get();
        FileResultData fileResultData = new FileResultData();
        fileResultData.setDocumentId(fileEntry.getDocumentId());
        fileResultData.setOriginalFileName(fileEntry.getOriginalFileName());
        fileResultData.setStatus(ocrData.getStatus().name());
        fileResultData.setErrorMessage(ocrData.getErrorMessage());
        fileResultData.setCreatedAt(ocrData.getCreatedAt());
        fileResultData.setExtractedText(ocrData.getExtractedText());

        return fileResultData;
    }
}
