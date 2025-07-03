package com.extractor.unraveldocs.documents.impl;

import com.extractor.unraveldocs.documents.dto.response.*;
import com.extractor.unraveldocs.documents.interfaces.GetDocumentService;
import com.extractor.unraveldocs.documents.model.DocumentCollection;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetDocumentServiceImpl implements GetDocumentService {

    private final DocumentCollectionRepository documentCollectionRepository;

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "documentCollection", key = "#collectionId")
    public DocumentCollectionResponse<GetDocumentCollectionData> getDocumentCollectionById(String collectionId, String userId) {
        DocumentCollection collection = documentCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("Document collection not found with ID: " + collectionId));

        if (!collection.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to view this document collection.");
        }

        List<FileEntryData> fileEntryDataList = collection.getFiles().stream()
                .map(this::mapToFileEntryData)
                .collect(Collectors.toList());

        GetDocumentCollectionData responseData = GetDocumentCollectionData.builder()
                .id(collection.getId())
                .userId(collection.getUser().getId())
                .collectionStatus(collection.getCollectionStatus())
                .uploadTimestamp(collection.getUploadTimestamp())
                .createdAt(collection.getCreatedAt())
                .updatedAt(collection.getUpdatedAt())
                .files(fileEntryDataList)
                .build();

        return DocumentCollectionResponse.<GetDocumentCollectionData>builder()
                .statusCode(HttpStatus.OK.value())
                .status("success")
                .message("Document collection retrieved successfully.")
                .data(responseData)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "documentCollections", key = "#userId")
    public DocumentCollectionResponse<List<DocumentCollectionSummary>> getAllDocumentCollectionsByUser(String userId) {
        List<DocumentCollection> collections = documentCollectionRepository.findAllByUserId(userId);

        List<DocumentCollectionSummary> summaries = collections.stream()
                .map(collection -> DocumentCollectionSummary.builder()
                        .id(collection.getId())
                        .collectionStatus(collection.getCollectionStatus())
                        .fileCount(collection.getFiles().size())
                        .uploadTimestamp(collection.getUploadTimestamp())
                        .createdAt(collection.getCreatedAt())
                        .updatedAt(collection.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());

        return DocumentCollectionResponse.<List<DocumentCollectionSummary>>builder()
                .statusCode(HttpStatus.OK.value())
                .status("success")
                .message("Document collections retrieved successfully.")
                .data(summaries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "fileEntry", key = "#collectionId + '-' + #documentId")
    public DocumentCollectionResponse<FileEntryData> getFileFromCollection(String collectionId, String documentId, String userId) {
        DocumentCollection collection = documentCollectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("Document collection not found with ID: " + collectionId));

        if (!collection.getUser().getId().equals(userId)) {
            throw new ForbiddenException("You are not authorized to access this document collection.");
        }

        FileEntry fileEntry = collection.getFiles().stream()
                .filter(file -> file.getDocumentId().equals(documentId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("File with document ID: " + documentId + " not found in collection: " + collectionId));

        FileEntryData fileEntryData = mapToFileEntryData(fileEntry);

        return DocumentCollectionResponse.<FileEntryData>builder()
                .statusCode(HttpStatus.OK.value())
                .status("success")
                .message("File retrieved successfully.")
                .data(fileEntryData)
                .build();
    }

    private FileEntryData mapToFileEntryData(FileEntry fileEntry) {
        return FileEntryData.builder()
                .documentId(fileEntry.getDocumentId())
                .originalFileName(fileEntry.getOriginalFileName())
                .fileSize(fileEntry.getFileSize())
                .fileUrl(fileEntry.getFileUrl())
                .status(fileEntry.getUploadStatus())
                .build();
    }
}