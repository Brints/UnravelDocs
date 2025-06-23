package com.extractor.unraveldocs.documents.service.impl;

import com.extractor.unraveldocs.auth.enums.Role;
import com.extractor.unraveldocs.config.DocumentConfigProperties;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionUploadData;
import com.extractor.unraveldocs.documents.dto.response.FileEntryData;
import com.extractor.unraveldocs.documents.enums.DocumentStatus;
import com.extractor.unraveldocs.documents.enums.DocumentUploadState;
import com.extractor.unraveldocs.documents.model.DocumentCollection;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.documents.utils.SanitizeLogging;
import com.extractor.unraveldocs.ocrprocessing.utils.FileStorageService;
import com.extractor.unraveldocs.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DocumentUploadImplTest {

    @Mock
    private DocumentCollectionRepository documentCollectionRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private DocumentConfigProperties documentConfigProperties;

    @Mock
    private SanitizeLogging s;

    @InjectMocks
    private DocumentUploadImpl documentUploadService;

    private User testUser;
    private MockMultipartFile validFile1;
    private MockMultipartFile validFile2;
    private MockMultipartFile invalidFileTypeFile;
    private MockMultipartFile emptyFile;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID().toString());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.USER);
        testUser.setVerified(true);
        testUser.setActive(true);

        validFile1 = new MockMultipartFile("files", "file1.png", "image/png", "file1 content".getBytes());
        validFile2 = new MockMultipartFile("files", "file2.jpg", "image/jpeg", "file2 content".getBytes());
        invalidFileTypeFile = new MockMultipartFile("files", "file3.txt", "text/plain", "file3 content".getBytes());
        emptyFile = new MockMultipartFile("files", "empty.png", "image/png", new byte[0]);

        when(s.sanitizeLogging(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void uploadDocuments_success_allFilesUploaded() {
        // Arrange
        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("image/png", "image/jpeg"));

        MultipartFile[] files = {validFile1, validFile2};
        String file1Url = "https://cloudinary.com/file1.png";
        String file2Url = "https://cloudinary.com/file2.jpg";
        String file1StorageId = "storageId1";
        String file2StorageId = "storageId2";

        FileEntry fileEntry1 = FileEntry.builder().documentId(UUID.randomUUID().toString())
                .originalFileName(validFile1.getOriginalFilename()).fileUrl(file1Url).storageId(file1StorageId)
                .uploadStatus(DocumentUploadState.SUCCESS.toString()).build();
        FileEntry fileEntry2 = FileEntry.builder().documentId(UUID.randomUUID().toString())
                .originalFileName(validFile2.getOriginalFilename()).fileUrl(file2Url).storageId(file2StorageId)
                .uploadStatus(DocumentUploadState.SUCCESS.toString()).build();

        when(fileStorageService.handleSuccessfulFileUpload(eq(validFile1), eq(validFile1.getOriginalFilename()), any()))
                .thenReturn(fileEntry1);
        when(fileStorageService.handleSuccessfulFileUpload(eq(validFile2), eq(validFile2.getOriginalFilename()), any()))
                .thenReturn(fileEntry2);

        DocumentCollection savedCollection = DocumentCollection.builder()
                .id(UUID.randomUUID().toString())
                .user(testUser)
                .files(new ArrayList<>(List.of(fileEntry1, fileEntry2)))
                .collectionStatus(DocumentStatus.COMPLETED)
                .uploadTimestamp(OffsetDateTime.now())
                .build();
        when(documentCollectionRepository.save(any(DocumentCollection.class))).thenReturn(savedCollection);

        // Act
        DocumentCollectionResponse<DocumentCollectionUploadData> response = documentUploadService.uploadDocuments(files, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("success", response.getStatus());
        assertEquals("All 2 document(s) uploaded successfully.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(savedCollection.getId(), response.getData().getCollectionId());
        assertEquals(DocumentStatus.COMPLETED, response.getData().getOverallStatus());
        assertEquals(2, response.getData().getFiles().size());

        FileEntryData file1Data = response.getData().getFiles().stream().filter(f -> f.getOriginalFileName().equals(validFile1.getOriginalFilename())).findFirst().orElse(null);
        assertNotNull(file1Data);
        assertEquals(DocumentUploadState.SUCCESS.toString(), file1Data.getStatus());
        assertEquals(file1Url, file1Data.getFileUrl());
        assertEquals(fileEntry1.getDocumentId(), file1Data.getDocumentId());


        ArgumentCaptor<DocumentCollection> collectionCaptor = ArgumentCaptor.forClass(DocumentCollection.class);
        verify(documentCollectionRepository).save(collectionCaptor.capture());
        DocumentCollection capturedCollection = collectionCaptor.getValue();
        assertEquals(2, capturedCollection.getFiles().size());
        assertTrue(capturedCollection
                .getFiles().stream()
                .allMatch(fe -> DocumentUploadState.SUCCESS.toString().equals(fe.getUploadStatus())));
        assertEquals(DocumentStatus.COMPLETED, capturedCollection.getCollectionStatus());
    }

    @Test
    void uploadDocuments_partialSuccess_oneFileFailsValidation() {
        // Arrange
        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("image/png", "image/jpeg"));

        MultipartFile[] files = {validFile1, invalidFileTypeFile};
        String file1Url = "https://cloudinary.com/file1.png";
        String file1StorageId = "storageId1";

        FileEntry fileEntry1 = FileEntry.builder().documentId(UUID.randomUUID().toString())
                .originalFileName(validFile1.getOriginalFilename()).fileUrl(file1Url).storageId(file1StorageId)
                .uploadStatus(DocumentUploadState.SUCCESS.toString()).build();

        when(fileStorageService.handleSuccessfulFileUpload(eq(validFile1), eq(validFile1.getOriginalFilename()), any()))
                .thenReturn(fileEntry1);

        DocumentCollection savedCollection = DocumentCollection.builder()
                .id(UUID.randomUUID().toString())
                .user(testUser)
                .files(new ArrayList<>(List.of(fileEntry1)))
                .collectionStatus(DocumentStatus.COMPLETED)
                .uploadTimestamp(OffsetDateTime.now())
                .build();
        when(documentCollectionRepository.save(any(DocumentCollection.class))).thenReturn(savedCollection);


        // Act
        DocumentCollectionResponse<DocumentCollectionUploadData> response = documentUploadService.uploadDocuments(files, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("partial_success", response.getStatus());
        assertEquals("1 document(s) uploaded successfully. 1 failed validation. 0 failed storage. Check individual statuses.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(savedCollection.getId(), response.getData().getCollectionId());
        assertEquals(DocumentStatus.PARTIALLY_COMPLETED, response.getData().getOverallStatus());
        assertEquals(2, response.getData().getFiles().size());

        FileEntryData successData = response.getData()
                .getFiles().stream()
                .filter(f -> DocumentUploadState.SUCCESS.toString()
                        .equals(f.getStatus())).findFirst().orElse(null);
        assertNotNull(successData);
        assertEquals(validFile1.getOriginalFilename(), successData.getOriginalFileName());

        FileEntryData failedData = response.getData()
                .getFiles().stream()
                .filter(f -> DocumentUploadState.FAILED_VALIDATION.toString()
                        .equals(f.getStatus())).findFirst().orElse(null);
        assertNotNull(failedData);
        assertEquals(invalidFileTypeFile.getOriginalFilename(), failedData.getOriginalFileName());
        assertNotNull(failedData.getDocumentId());

        ArgumentCaptor<DocumentCollection> collectionCaptor = ArgumentCaptor.forClass(DocumentCollection.class);
        verify(documentCollectionRepository).save(collectionCaptor.capture());
        DocumentCollection capturedCollection = collectionCaptor.getValue();
        assertEquals(1, capturedCollection.getFiles().size());
        assertEquals(DocumentStatus.COMPLETED, capturedCollection.getCollectionStatus());
    }

    @Test
    void uploadDocuments_partialSuccess_oneFileFailsStorageUpload() {
        // Arrange
        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("image/png", "image/jpeg"));

        MultipartFile[] files = {validFile1, validFile2};
        String file1Url = "https://cloudinary.com/file1.png";
        String file1StorageId = "storageId1";

        FileEntry fileEntry1 = FileEntry.builder().documentId(UUID.randomUUID().toString())
                .originalFileName(validFile1.getOriginalFilename()).fileUrl(file1Url).storageId(file1StorageId)
                .uploadStatus(DocumentUploadState.SUCCESS.toString()).build();

        when(fileStorageService.handleSuccessfulFileUpload(eq(validFile1), eq(validFile1.getOriginalFilename()), any()))
                .thenReturn(fileEntry1);
        when(fileStorageService.handleSuccessfulFileUpload(eq(validFile2), eq(validFile2.getOriginalFilename()), any()))
                .thenThrow(new RuntimeException("Cloudinary down"));


        DocumentCollection savedCollection = DocumentCollection.builder()
                .id(UUID.randomUUID().toString())
                .user(testUser)
                .files(new ArrayList<>(List.of(
                        fileEntry1,
                        FileEntry.builder()
                                .documentId(UUID.randomUUID().toString())
                                .originalFileName(validFile2.getOriginalFilename())
                                .uploadStatus(DocumentUploadState.FAILED_STORAGE_UPLOAD.toString())
                                .errorMessage("Storage upload failed: Cloudinary down").build()
                )))
                .collectionStatus(DocumentStatus.PARTIALLY_COMPLETED)
                .uploadTimestamp(OffsetDateTime.now())
                .build();
        when(documentCollectionRepository.save(any(DocumentCollection.class))).thenReturn(savedCollection);

        // Act
        DocumentCollectionResponse<DocumentCollectionUploadData> response = documentUploadService.uploadDocuments(files, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("partial_success", response.getStatus());
        assertEquals("1 document(s) uploaded successfully. 0 failed validation. 1 failed storage. Check individual statuses.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(savedCollection.getId(), response.getData().getCollectionId());
        assertEquals(DocumentStatus.PARTIALLY_COMPLETED, response.getData().getOverallStatus());
        assertEquals(2, response.getData().getFiles().size());

        FileEntryData successData = response
                .getData()
                .getFiles().stream()
                .filter(f -> DocumentUploadState.SUCCESS.toString()
                        .equals(f.getStatus())).findFirst().orElse(null);
        assertNotNull(successData);
        assertEquals(validFile1.getOriginalFilename(), successData.getOriginalFileName());

        FileEntryData failedData = response
                .getData()
                .getFiles().stream()
                .filter(f -> DocumentUploadState.FAILED_STORAGE.toString()
                        .equals(f.getStatus())).findFirst().orElse(null);
        assertNotNull(failedData);
        assertEquals(validFile2.getOriginalFilename(), failedData.getOriginalFileName());
        assertNotNull(failedData.getDocumentId());

        ArgumentCaptor<DocumentCollection> collectionCaptor = ArgumentCaptor.forClass(DocumentCollection.class);
        verify(documentCollectionRepository).save(collectionCaptor.capture());
        DocumentCollection capturedCollection = collectionCaptor.getValue();
        assertEquals(2, capturedCollection.getFiles().size());
        assertEquals(DocumentStatus.PARTIALLY_COMPLETED, capturedCollection.getCollectionStatus());
        assertTrue(capturedCollection
                .getFiles().stream()
                .anyMatch(fe -> DocumentUploadState.FAILED_STORAGE_UPLOAD.toString()
                        .equals(fe.getUploadStatus())));
    }


    @Test
    void uploadDocuments_failure_allFilesFailValidation() {
        // Arrange
        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("image/png", "image/jpeg"));

        MultipartFile[] files = {invalidFileTypeFile, emptyFile};

        // Act
        DocumentCollectionResponse<DocumentCollectionUploadData> response = documentUploadService.uploadDocuments(files, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("failure", response.getStatus());
        assertEquals("All 2 document(s) failed validation. No documents were uploaded.", response.getMessage());
        assertNotNull(response.getData());
        assertNull(response.getData().getCollectionId());
        assertEquals(DocumentStatus.FAILED_UPLOAD, response.getData().getOverallStatus());
        assertEquals(2, response.getData().getFiles().size());
        assertTrue(response
                .getData()
                .getFiles().stream()
                .allMatch(f -> DocumentUploadState.FAILED_VALIDATION.toString()
                        .equals(f.getStatus())));

        verify(documentCollectionRepository, never()).save(any(DocumentCollection.class));
    }

    @Test
    void uploadDocuments_failure_allFilesFailStorageUpload() {
        // Arrange
        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("image/png", "image/jpeg"));

        MultipartFile[] files = {validFile1, validFile2};

        when(fileStorageService.handleSuccessfulFileUpload(any(MultipartFile.class), anyString(), any()))
                .thenThrow(new RuntimeException("Cloudinary down"));

        DocumentCollection savedCollection = DocumentCollection.builder()
                .id(UUID.randomUUID().toString())
                .user(testUser)
                .files(new ArrayList<>())
                .collectionStatus(DocumentStatus.FAILED_UPLOAD)
                .uploadTimestamp(OffsetDateTime.now())
                .build();
        when(documentCollectionRepository.save(any(DocumentCollection.class))).thenReturn(savedCollection);

        // Act
        DocumentCollectionResponse<DocumentCollectionUploadData> response = documentUploadService.uploadDocuments(files, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("failure", response.getStatus());
        assertEquals("No documents were successfully uploaded. 0 failed validation. 2 failed storage. Check individual statuses.", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(savedCollection.getId(), response.getData().getCollectionId());
        assertEquals(DocumentStatus.PARTIALLY_COMPLETED, response.getData().getOverallStatus());
        assertEquals(2, response.getData().getFiles().size());
        assertTrue(response
                .getData()
                .getFiles().stream()
                .allMatch(f -> DocumentUploadState.FAILED_STORAGE.toString().equals(f.getStatus())));

        ArgumentCaptor<DocumentCollection> collectionCaptor = ArgumentCaptor.forClass(DocumentCollection.class);
        verify(documentCollectionRepository).save(collectionCaptor.capture());
        DocumentCollection capturedCollection = collectionCaptor.getValue();
        assertEquals(2, capturedCollection.getFiles().size());
        assertEquals(DocumentStatus.FAILED_UPLOAD, capturedCollection.getCollectionStatus());
        assertTrue(capturedCollection.
                getFiles().stream()
                .allMatch(fe -> DocumentUploadState.FAILED_STORAGE_UPLOAD.toString()
                        .equals(fe.getUploadStatus())));
    }

    @Test
    void uploadDocuments_failure_noFilesProvided() {
        // Arrange
        MultipartFile[] files = {};

        // Act
        DocumentCollectionResponse<DocumentCollectionUploadData> response = documentUploadService.uploadDocuments(files, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("failure", response.getStatus());
        assertEquals("No files provided for upload.", response.getMessage());
        assertNotNull(response.getData());
        assertNull(response.getData().getCollectionId());
        assertEquals(DocumentStatus.FAILED_UPLOAD, response.getData().getOverallStatus());
        assertEquals(Collections.emptyList(), response.getData().getFiles());

        verify(documentCollectionRepository, never()).save(any(DocumentCollection.class));
    }
}