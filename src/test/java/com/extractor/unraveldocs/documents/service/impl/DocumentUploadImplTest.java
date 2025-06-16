package com.extractor.unraveldocs.documents.service.impl;

import com.extractor.unraveldocs.config.DocumentConfigProperties;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.documents.dto.response.FileEntryData;
import com.extractor.unraveldocs.documents.enums.DocumentStatus;
import com.extractor.unraveldocs.documents.model.DocumentCollection;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.NotFoundException;
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
import com.extractor.unraveldocs.auth.enums.Role; // Import Role

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    }

    @Test
    void uploadDocuments_success_allFilesUploaded() throws Exception {
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
    void uploadDocuments_partialSuccess_oneFileFailsValidation() throws Exception {
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
    void uploadDocuments_partialSuccess_oneFileFailsStorageUpload() throws Exception {
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

    @Test
    void deleteDocument_success() {
        // Arrange
        String collectionId = UUID.randomUUID().toString();
        String file1StorageId = "storageId1";
        String file1DocumentId = UUID.randomUUID().toString();

        FileEntry fileEntry1 = FileEntry.builder().documentId(file1DocumentId).storageId(file1StorageId).uploadStatus("SUCCESS").build();
        DocumentCollection collection = DocumentCollection.builder()
                .id(collectionId)
                .user(testUser)
                .files(new ArrayList<>(List.of(fileEntry1)))
                .build();
        when(documentCollectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        doNothing().when(cloudinaryService).deleteFile(file1StorageId);
        doNothing().when(documentCollectionRepository).delete(collection);

        // Act
        DocumentCollectionResponse response = documentUploadService.deleteDocument(collectionId, testUser.getId());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertEquals("success", response.getStatus());
        assertEquals("Document collection deleted successfully.", response.getMessage());
        assertNull(response.getData());

        verify(cloudinaryService).deleteFile(file1StorageId);
        verify(documentCollectionRepository).delete(collection);
    }

    @Test
    void deleteDocument_collectionNotFound() {
        // Arrange
        String collectionId = UUID.randomUUID().toString();
        when(documentCollectionRepository.findById(collectionId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            documentUploadService.deleteDocument(collectionId, testUser.getId());
        });
        assertEquals("Document collection not found with ID: " + collectionId, exception.getMessage());
        verify(cloudinaryService, never()).deleteFile(anyString());
        verify(documentCollectionRepository, never()).delete(any(DocumentCollection.class));
    }

    @Test
    void deleteDocument_forbidden() {
        // Arrange
        String collectionId = UUID.randomUUID().toString();
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID().toString());
        anotherUser.setEmail("another@example.com");
        anotherUser.setRole(Role.USER);
        anotherUser.setVerified(true);
        anotherUser.setActive(true);

        DocumentCollection collection = DocumentCollection.builder().id(collectionId).user(anotherUser).files(new ArrayList<>()).build();
        when(documentCollectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            documentUploadService.deleteDocument(collectionId, testUser.getId());
        });
        assertEquals("You are not authorized to delete this document collection.", exception.getMessage());
        verify(cloudinaryService, never()).deleteFile(anyString());
        verify(documentCollectionRepository, never()).delete(any(DocumentCollection.class));
    }

    @Test
    void deleteFileFromCollection_success() {
        // Arrange
        String collectionId = UUID.randomUUID().toString();
        String documentIdToRemove = UUID.randomUUID().toString();
        String storageIdToRemove = "storageIdToRemove";

        FileEntry fileToRemove = FileEntry.builder().documentId(documentIdToRemove).storageId(storageIdToRemove).uploadStatus("SUCCESS").build();
        FileEntry remainingFile = FileEntry.builder().documentId(UUID.randomUUID().toString()).storageId("otherStorageId").uploadStatus("SUCCESS").build();
        List<FileEntry> files = new ArrayList<>(List.of(fileToRemove, remainingFile));

        DocumentCollection collection = DocumentCollection.builder()
                .id(collectionId)
                .user(testUser)
                .files(files) // Use the mutable list
                .collectionStatus(DocumentStatus.COMPLETED)
                .build();

        when(documentCollectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        doNothing().when(cloudinaryService).deleteFile(storageIdToRemove);
        when(documentCollectionRepository.save(any(DocumentCollection.class))).thenAnswer(invocation -> invocation.getArgument(0));


        // Act
        DocumentCollectionResponse response = documentUploadService.deleteFileFromCollection(collectionId, documentIdToRemove, testUser.getId());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertEquals("success", response.getStatus());
        assertEquals("File with document ID " + documentIdToRemove + " deleted successfully from collection " + collectionId, response.getMessage());
        assertNull(response.getData());

        verify(cloudinaryService).deleteFile(storageIdToRemove);

        ArgumentCaptor<DocumentCollection> collectionCaptor = ArgumentCaptor.forClass(DocumentCollection.class);
        verify(documentCollectionRepository).save(collectionCaptor.capture());
        DocumentCollection savedCollection = collectionCaptor.getValue();

        assertEquals(1, savedCollection.getFiles().size());
        assertFalse(savedCollection.getFiles().contains(fileToRemove));
        assertEquals(DocumentStatus.COMPLETED, savedCollection.getCollectionStatus());
    }

    @Test
    void deleteFileFromCollection_lastFileDeletesCollection() {
        // Arrange
        String collectionId = UUID.randomUUID().toString();
        String documentIdToRemove = UUID.randomUUID().toString();
        String storageIdToRemove = "storageIdToRemove";

        FileEntry fileToRemove = FileEntry.builder().documentId(documentIdToRemove).storageId(storageIdToRemove).uploadStatus("SUCCESS").build();
        List<FileEntry> files = new ArrayList<>(List.of(fileToRemove)); // Mutable list

        DocumentCollection collection = DocumentCollection.builder()
                .id(collectionId)
                .user(testUser)
                .files(files) // Use the mutable list
                .collectionStatus(DocumentStatus.COMPLETED)
                .build();

        when(documentCollectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));
        doNothing().when(cloudinaryService).deleteFile(storageIdToRemove);
        doNothing().when(documentCollectionRepository).delete(any(DocumentCollection.class));


        // Act
        DocumentCollectionResponse response = documentUploadService.deleteFileFromCollection(collectionId, documentIdToRemove, testUser.getId());

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT.value(), response.getStatusCode());
        assertEquals("success", response.getStatus());
        assertEquals("File with document ID " + documentIdToRemove + " deleted successfully from collection " + collectionId, response.getMessage());
        assertNull(response.getData());

        verify(cloudinaryService).deleteFile(storageIdToRemove);
        verify(documentCollectionRepository).delete(collection);
        verify(documentCollectionRepository, never()).save(any(DocumentCollection.class));
        assertTrue(collection.getFiles().isEmpty());
    }

    @Test
    void deleteFileFromCollection_fileNotFoundInCollection() {
        // Arrange
        String collectionId = UUID.randomUUID().toString();
        String nonExistentDocumentId = UUID.randomUUID().toString();
        DocumentCollection collection = DocumentCollection.builder().id(collectionId).user(testUser).files(new ArrayList<>()).build();
        when(documentCollectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            documentUploadService.deleteFileFromCollection(collectionId, nonExistentDocumentId, testUser.getId());
        });
        assertEquals("File with document ID: " + nonExistentDocumentId + " not found in collection: " + collectionId, exception.getMessage());
        verify(cloudinaryService, never()).deleteFile(anyString());
    }

    @Test
    void deleteFileFromCollection_collectionNotFound() {
        // Arrange
        String collectionId = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        when(documentCollectionRepository.findById(collectionId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            documentUploadService.deleteFileFromCollection(collectionId, documentId, testUser.getId());
        });
        assertEquals("Document collection not found with ID: " + collectionId, exception.getMessage());
    }

    @Test
    void deleteFileFromCollection_forbidden() {
        // Arrange
        String collectionId = UUID.randomUUID().toString();
        String documentId = UUID.randomUUID().toString();
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID().toString());
        anotherUser.setEmail("another@example.com");
        anotherUser.setRole(Role.USER);
        anotherUser.setVerified(true);
        anotherUser.setActive(true);

        FileEntry existingFile = FileEntry.builder().documentId(documentId).storageId("someStorageId").build();
        DocumentCollection collection = DocumentCollection.builder()
                .id(collectionId)
                .user(anotherUser)
                .files(new ArrayList<>(List.of(existingFile)))
                .build();
        when(documentCollectionRepository.findById(collectionId)).thenReturn(Optional.of(collection));

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () -> {
            documentUploadService.deleteFileFromCollection(collectionId, documentId, testUser.getId());
        });
        assertEquals("You are not authorized to modify this document collection.", exception.getMessage());
        verify(cloudinaryService, never()).deleteFile(anyString());
    }
}