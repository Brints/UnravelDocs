package com.extractor.unraveldocs.wordexport.impl;

import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import com.extractor.unraveldocs.ocrprocessing.enums.OcrStatus;
import com.extractor.unraveldocs.ocrprocessing.model.OcrData;
import com.extractor.unraveldocs.ocrprocessing.repository.OcrDataRepository;
import com.extractor.unraveldocs.ocrprocessing.utils.FindAndValidateFileEntry;
import com.extractor.unraveldocs.wordexport.interfaces.DocxExportService;
import com.extractor.unraveldocs.wordexport.interfaces.DownloadOcrResultService;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class DownloadOcrServiceImpl implements DownloadOcrResultService {
    private final DocumentCollectionRepository documentCollectionRepository;
    private final OcrDataRepository ocrDataRepository;
    private final FindAndValidateFileEntry validateFileEntry;
    private final DocxExportService docxExportService;

    @Override
    @Transactional(readOnly = true)
    public DownloadableFile downloadAsDocx(String collectionId, String documentId, String userId) {
        FileEntry fileEntry = validateFileEntry
                .findAndValidateFileEntry(collectionId, documentId, userId, documentCollectionRepository);

        OcrData ocrData = ocrDataRepository.findByDocumentId(documentId)
                .orElseThrow(() -> new NotFoundException("OCR data not found for document: " + documentId));

        if (ocrData.getStatus() != OcrStatus.COMPLETED) {
            throw new BadRequestException("OCR processing is not completed for document: " + documentId);
        }

        if (ocrData.getExtractedText() == null || ocrData.getExtractedText().isBlank()) {
            throw new BadRequestException("No text extracted from the document: " + documentId);
        }

        try {
            InputStreamResource resource = new InputStreamResource(docxExportService.generateDocxFromText(ocrData.getExtractedText()));
            String originalFileName = fileEntry.getOriginalFileName();
            String docxFileName = originalFileName.substring(0, originalFileName.lastIndexOf('.')) + ".docx";

            return new DownloadableFile(docxFileName, resource);
        } catch (IOException e) {
            throw new ServiceException("Error generating DOCX file for document: " + documentId, e);
        }
    }
}
