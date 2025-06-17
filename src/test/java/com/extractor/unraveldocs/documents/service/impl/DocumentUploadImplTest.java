package com.extractor.unraveldocs.documents.service.impl;

import com.extractor.unraveldocs.config.DocumentConfigProperties;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.documents.dto.response.FileEntryData;
import com.extractor.unraveldocs.documents.enums.DocumentStatus;
import com.extractor.unraveldocs.documents.model.DocumentCollection;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.documents.utils.SanitizeLogging;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.utils.imageupload.cloudinary.CloudinaryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import com.extractor.unraveldocs.auth.enums.Role;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentUploadImplTest {

    @Mock
    private DocumentCollectionRepository documentCollectionRepository;

    @Mock
    private CloudinaryService cloudinaryService;

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
        when(documentConfigProperties.getStorageFolder()).thenReturn("test-folder");

        MultipartFile[] files = {validFile1, validFile2};
        String file1Url = "https://cloudinary.com/file1.png";
        String file2Url = "https://cloudinary.com/file2.jpg";
        String file1StorageId = "storageId1";
        String file2StorageId = "storageId2";

        when(cloudinaryService.uploadFile(eq(validFile1), anyString(), eq(validFile1.getOriginalFilename()), anyString())).thenReturn(file1Url);
        when(cloudinaryService.generateRandomPublicId(eq(validFile1.getOriginalFilename()))).thenReturn(file1StorageId);
        when(cloudinaryService.uploadFile(eq(validFile2), anyString(), eq(validFile2.getOriginalFilename()), anyString())).thenReturn(file2Url);
        when(cloudinaryService.generateRandomPublicId(eq(validFile2.getOriginalFilename()))).thenReturn(file2StorageId);

        DocumentCollection savedCollection = DocumentCollection.builder()
                .id(UUID.randomUUID().toString())
                .user(testUser)
                .files(new ArrayList<>(List.of(
                        FileEntry.builder().documentId(UUID.randomUUID().toString()).originalFileName(validFile1.getOriginalFilename()).fileUrl(file1Url).storageId(file1StorageId).uploadStatus("SUCCESS").build(),
                        FileEntry.builder().documentId(UUID.randomUUID().toString()).originalFileName(validFile2.getOriginalFilename()).fileUrl(file2Url).storageId(file2StorageId).uploadStatus("SUCCESS").build()
                )))
                .collectionStatus(DocumentStatus.COMPLETED)
                .uploadTimestamp(OffsetDateTime.now())
                .build();
        when(documentCollectionRepository.save(any(DocumentCollection.class))).thenReturn(savedCollection);

        // Act
        DocumentCollectionResponse response = documentUploadService.uploadDocuments(files, testUser);

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
        assertEquals("SUCCESS", file1Data.getStatus());
        assertEquals(file1Url, file1Data.getFileUrl());
        assertNotNull(file1Data.getDocumentId());


        ArgumentCaptor<DocumentCollection> collectionCaptor = ArgumentCaptor.forClass(DocumentCollection.class);
        verify(documentCollectionRepository).save(collectionCaptor.capture());
        DocumentCollection capturedCollection = collectionCaptor.getValue();
        assertEquals(2, capturedCollection.getFiles().size());
        assertTrue(capturedCollection.getFiles().stream().allMatch(fe -> "SUCCESS".equals(fe.getUploadStatus())));
        assertEquals(DocumentStatus.COMPLETED, capturedCollection.getCollectionStatus());
    }

    @Test
    void uploadDocuments_partialSuccess_oneFileFailsValidation() {
        // Arrange
        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("image/png", "image/jpeg"));
        when(documentConfigProperties.getStorageFolder()).thenReturn("test-folder");

        MultipartFile[] files = {validFile1, invalidFileTypeFile};
        String file1Url = "https://cloudinary.com/file1.png";
        String file1StorageId = "storageId1";

        when(cloudinaryService.uploadFile(eq(validFile1), anyString(), eq(validFile1.getOriginalFilename()), anyString())).thenReturn(file1Url);
        when(cloudinaryService.generateRandomPublicId(eq(validFile1.getOriginalFilename()))).thenReturn(file1StorageId);

        DocumentCollection savedCollection = DocumentCollection.builder()
                .id(UUID.randomUUID().toString())
                .user(testUser)
                .files(new ArrayList<>(List.of(
                        FileEntry.builder().documentId(UUID.randomUUID().toString()).originalFileName(validFile1.getOriginalFilename()).fileUrl(file1Url).storageId(file1StorageId).uploadStatus("SUCCESS").build()
                )))
                .collectionStatus(DocumentStatus.COMPLETED) // Only successful files are in processedFileEntries
                .uploadTimestamp(OffsetDateTime.now())
                .build();
        when(documentCollectionRepository.save(any(DocumentCollection.class))).thenReturn(savedCollection);


        // Act
        DocumentCollectionResponse response = documentUploadService.uploadDocuments(files, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("partial_success", response.getStatus());
        assertTrue(response.getMessage().contains("1 document(s) uploaded successfully. 1 failed validation."));
        assertNotNull(response.getData());
        assertEquals(savedCollection.getId(), response.getData().getCollectionId());
        assertEquals(DocumentStatus.PARTIALLY_COMPLETED, response.getData().getOverallStatus());
        assertEquals(2, response.getData().getFiles().size());

        FileEntryData successData = response.getData().getFiles().stream().filter(f -> "SUCCESS".equals(f.getStatus())).findFirst().orElse(null);
        assertNotNull(successData);
        assertEquals(validFile1.getOriginalFilename(), successData.getOriginalFileName());

        FileEntryData failedData = response.getData().getFiles().stream().filter(f -> "FAILED_VALIDATION".equals(f.getStatus())).findFirst().orElse(null);
        assertNotNull(failedData);
        assertEquals(invalidFileTypeFile.getOriginalFilename(), failedData.getOriginalFileName());
        assertNotNull(failedData.getDocumentId());

        ArgumentCaptor<DocumentCollection> collectionCaptor = ArgumentCaptor.forClass(DocumentCollection.class);
        verify(documentCollectionRepository).save(collectionCaptor.capture());
        DocumentCollection capturedCollection = collectionCaptor.getValue();
        assertEquals(1, capturedCollection.getFiles().size()); // Only one file was processed and added to the collection
        assertEquals(DocumentStatus.COMPLETED, capturedCollection.getCollectionStatus());
    }

    @Test
    void uploadDocuments_partialSuccess_oneFileFailsStorageUpload() {
        // Arrange
        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("image/png", "image/jpeg"));
        when(documentConfigProperties.getStorageFolder()).thenReturn("test-folder");

        MultipartFile[] files = {validFile1, validFile2};
        String file1Url = "https://cloudinary.com/file1.png";
        String file1StorageId = "storageId1";

        when(cloudinaryService.uploadFile(eq(validFile1), anyString(), eq(validFile1.getOriginalFilename()), anyString())).thenReturn(file1Url);
        when(cloudinaryService.generateRandomPublicId(eq(validFile1.getOriginalFilename()))).thenReturn(file1StorageId);
        // For validFile2, uploadFile will throw an exception.
        // The generateRandomPublicId for validFile2 will not be called if uploadFile fails.
        when(cloudinaryService.uploadFile(eq(validFile2), anyString(), eq(validFile2.getOriginalFilename()), anyString())).thenThrow(new RuntimeException("Cloudinary down"));


        DocumentCollection savedCollection = DocumentCollection.builder()
                .id(UUID.randomUUID().toString())
                .user(testUser)
                .files(new ArrayList<>(List.of( // This list will contain both entries, one success, one failed_storage
                        FileEntry.builder().documentId(UUID.randomUUID().toString()).originalFileName(validFile1.getOriginalFilename()).fileUrl(file1Url).storageId(file1StorageId).uploadStatus("SUCCESS").build(),
                        FileEntry.builder().documentId(UUID.randomUUID().toString()).originalFileName(validFile2.getOriginalFilename()).uploadStatus("FAILED_STORAGE_UPLOAD").errorMessage("Storage upload failed: Cloudinary down").build()
                )))
                .collectionStatus(DocumentStatus.PARTIALLY_COMPLETED)
                .uploadTimestamp(OffsetDateTime.now())
                .build();
        when(documentCollectionRepository.save(any(DocumentCollection.class))).thenReturn(savedCollection);

        // Act
        DocumentCollectionResponse response = documentUploadService.uploadDocuments(files, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("partial_success", response.getStatus());
        assertTrue(response.getMessage().contains("1 document(s) uploaded successfully. 0 failed validation. 1 failed storage."));
        assertNotNull(response.getData());
        assertEquals(savedCollection.getId(), response.getData().getCollectionId());
        assertEquals(DocumentStatus.PARTIALLY_COMPLETED, response.getData().getOverallStatus());
        assertEquals(2, response.getData().getFiles().size());

        FileEntryData successData = response.getData().getFiles().stream().filter(f -> "SUCCESS".equals(f.getStatus())).findFirst().orElse(null);
        assertNotNull(successData);
        assertEquals(validFile1.getOriginalFilename(), successData.getOriginalFileName());

        FileEntryData failedData = response.getData().getFiles().stream().filter(f -> "FAILED_STORAGE".equals(f.getStatus())).findFirst().orElse(null);
        assertNotNull(failedData);
        assertEquals(validFile2.getOriginalFilename(), failedData.getOriginalFileName());
        assertNotNull(failedData.getDocumentId());

        ArgumentCaptor<DocumentCollection> collectionCaptor = ArgumentCaptor.forClass(DocumentCollection.class);
        verify(documentCollectionRepository).save(collectionCaptor.capture());
        DocumentCollection capturedCollection = collectionCaptor.getValue();
        assertEquals(2, capturedCollection.getFiles().size());
        assertEquals(DocumentStatus.PARTIALLY_COMPLETED, capturedCollection.getCollectionStatus());
        assertTrue(capturedCollection.getFiles().stream().anyMatch(fe -> "FAILED_STORAGE_UPLOAD".equals(fe.getUploadStatus())));
    }


    @Test
    void uploadDocuments_failure_allFilesFailValidation() {
        // Arrange
        when(documentConfigProperties.getAllowedFileTypes()).thenReturn(List.of("image/png", "image/jpeg"));

        MultipartFile[] files = {invalidFileTypeFile, emptyFile};

        // Act
        DocumentCollectionResponse response = documentUploadService.uploadDocuments(files, testUser);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        assertEquals("failure", response.getStatus());
        assertEquals("All 2 document(s) failed validation. No documents were uploaded.", response.getMessage());
        assertNotNull(response.getData());
        assertNull(response.getData().getCollectionId());
        assertEquals(DocumentStatus.FAILED_UPLOAD, response.getData().getOverallStatus());
        assertEquals(2, response.getData().getFiles().size());
        assertTrue(response.getData().getFiles().stream().allMatch(f -> "FAILED_VALIDATION".equals(f.getStatus())));

        verify(documentCollectionRepository, never()).save(any(DocumentCollection.class));
    }


}