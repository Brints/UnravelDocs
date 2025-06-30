package com.extractor.unraveldocs.ocrprocessing.service.impl;

import com.extractor.unraveldocs.config.DocumentConfigProperties;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionUploadData;
import com.extractor.unraveldocs.documents.enums.DocumentStatus;
import com.extractor.unraveldocs.documents.enums.DocumentUploadState;
import com.extractor.unraveldocs.documents.model.DocumentCollection;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.documents.utils.SanitizeLogging;
import com.extractor.unraveldocs.ocrprocessing.repository.OcrDataRepository;
import com.extractor.unraveldocs.ocrprocessing.utils.FileStorageService;
import com.extractor.unraveldocs.user.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkDocumentUploadExtractionImplTest {

    @Mock
    private DocumentConfigProperties documentConfigProperties;
    @Mock
    private DocumentCollectionRepository documentCollectionRepository;
    @Mock
    private OcrDataRepository ocrDataRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private SanitizeLogging s;
    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private BulkDocumentUploadExtractionImpl bulkDocumentUploadExtractionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID().toString());
        // Mock the static TransactionSynchronizationManager
        TransactionSynchronizationManager.initSynchronization();
        // Mock sanitize logging to simplify assertions
        when(s.sanitizeLogging(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    void uploadDocuments_Success() {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile("files", "test1.pdf", "application/pdf", "content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "test2.png", "image/png", "content".getBytes());
        MultipartFile[] files = {file1, file2};

        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("application/pdf", "image/png"));

        FileEntry fileEntry1 = FileEntry.builder().documentId(UUID.randomUUID().toString()).fileUrl("url1").uploadStatus(DocumentUploadState.SUCCESS.toString()).build();
        FileEntry fileEntry2 = FileEntry.builder().documentId(UUID.randomUUID().toString()).fileUrl("url2").uploadStatus(DocumentUploadState.SUCCESS.toString()).build();

        when(fileStorageService.handleSuccessfulFileUpload(eq(file1), anyString())).thenReturn(fileEntry1);
        when(fileStorageService.handleSuccessfulFileUpload(eq(file2), anyString())).thenReturn(fileEntry2);

        DocumentCollection savedCollection = new DocumentCollection();
        savedCollection.setId(UUID.randomUUID().toString());
        savedCollection.setFiles(List.of(fileEntry1, fileEntry2));
        savedCollection.setCollectionStatus(DocumentStatus.PROCESSING);
        when(documentCollectionRepository.save(any(DocumentCollection.class))).thenReturn(savedCollection);

        // Act
        DocumentCollectionResponse<DocumentCollectionUploadData> response = bulkDocumentUploadExtractionService.uploadDocuments(files, user);

        // Capture and invoke afterCommit to simulate transaction completion
        verify(documentCollectionRepository).save(any(DocumentCollection.class));
        TransactionSynchronizationManager.getSynchronizations().getFirst().afterCommit();


        // Assert
        assertNotNull(response);
        assertEquals(202, response.getStatusCode());
        assertEquals("2 document(s) uploaded successfully and queued for processing. 0 failed.", response.getMessage());
        assertNotNull(response.getData().getCollectionId());
        assertEquals(DocumentStatus.PROCESSING, response.getData().getOverallStatus());
        assertEquals(2, response.getData().getFiles().size());
        assertEquals(DocumentUploadState.SUCCESS.toString(), response.getData().getFiles().getFirst().getStatus());

        verify(documentCollectionRepository, times(1)).save(any(DocumentCollection.class));
        verify(ocrDataRepository, times(1)).saveAll(anyList());
        verify(rabbitTemplate, times(2)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void uploadDocuments_PartialSuccessWithFailures() {
        // Arrange
        MockMultipartFile successFile = new MockMultipartFile("files", "success.pdf", "application/pdf", "content".getBytes());
        MockMultipartFile validationFailFile = new MockMultipartFile("files", "fail.txt", "text/plain", "content".getBytes());
        MockMultipartFile storageFailFile = new MockMultipartFile("files", "storage_fail.pdf", "application/pdf", "content".getBytes());
        MultipartFile[] files = {successFile, validationFailFile, storageFailFile};

        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("application/pdf"));

        FileEntry successFileEntry = FileEntry.builder().documentId(UUID.randomUUID().toString()).fileUrl("url_success").uploadStatus(DocumentUploadState.SUCCESS.toString()).build();
        when(fileStorageService.handleSuccessfulFileUpload(eq(successFile), anyString())).thenReturn(successFileEntry);
        when(fileStorageService.handleSuccessfulFileUpload(eq(storageFailFile), anyString())).thenThrow(new RuntimeException("Storage unavailable"));

        DocumentCollection savedCollection = new DocumentCollection();
        savedCollection.setId(UUID.randomUUID().toString());
        savedCollection.setFiles(List.of(successFileEntry)); // Only the successful one
        savedCollection.setCollectionStatus(DocumentStatus.PROCESSING);
        when(documentCollectionRepository.save(any(DocumentCollection.class))).thenReturn(savedCollection);

        // Act
        DocumentCollectionResponse<DocumentCollectionUploadData> response = bulkDocumentUploadExtractionService.uploadDocuments(files, user);
        TransactionSynchronizationManager.getSynchronizations().getFirst().afterCommit();

        // Assert
        assertEquals(202, response.getStatusCode());
        assertEquals("1 document(s) uploaded successfully and queued for processing. 2 failed.", response.getMessage());
        assertEquals(DocumentStatus.PROCESSING, response.getData().getOverallStatus());
        assertEquals(3, response.getData().getFiles().size());

        // Check statuses
        assertTrue(response.getData().getFiles().stream().anyMatch(f -> DocumentUploadState.SUCCESS.toString().equals(f.getStatus()) && f.getOriginalFileName().equals("success.pdf")));
        assertTrue(response.getData().getFiles().stream().anyMatch(f -> DocumentUploadState.FAILED_VALIDATION.toString().equals(f.getStatus()) && f.getOriginalFileName().equals("fail.txt")));
        assertTrue(response.getData().getFiles().stream().anyMatch(f -> DocumentUploadState.FAILED_STORAGE_UPLOAD.toString().equals(f.getStatus()) && f.getOriginalFileName().equals("storage_fail.pdf")));

        verify(documentCollectionRepository, times(1)).save(any(DocumentCollection.class));
        verify(ocrDataRepository, times(1)).saveAll(anyList());
        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void uploadDocuments_AllFailValidation() {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile("files", "fail1.txt", "text/plain", "content".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("files", "fail2.exe", "application/octet-stream", "content".getBytes());
        MultipartFile[] files = {file1, file2};

        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("application/pdf"));

        // Act
        DocumentCollectionResponse<DocumentCollectionUploadData> response = bulkDocumentUploadExtractionService.uploadDocuments(files, user);

        // Assert
        assertEquals(202, response.getStatusCode());
        assertEquals("All document uploads failed.", response.getMessage());
        assertNull(response.getData().getCollectionId());
        assertEquals(DocumentStatus.FAILED_UPLOAD, response.getData().getOverallStatus());
        assertEquals(2, response.getData().getFiles().size());
        assertEquals(DocumentUploadState.FAILED_VALIDATION.toString(), response.getData().getFiles().get(0).getStatus());
        assertEquals(DocumentUploadState.FAILED_VALIDATION.toString(), response.getData().getFiles().get(1).getStatus());

        verify(documentCollectionRepository, never()).save(any());
        verify(ocrDataRepository, never()).saveAll(any());
        verify(fileStorageService, never()).handleSuccessfulFileUpload(any(), any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }
}