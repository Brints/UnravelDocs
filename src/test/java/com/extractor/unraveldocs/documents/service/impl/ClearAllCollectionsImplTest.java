package com.extractor.unraveldocs.documents.service.impl;

import com.extractor.unraveldocs.documents.impl.ClearAllCollectionsImpl;
import com.extractor.unraveldocs.documents.model.DocumentCollection;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.utils.imageupload.aws.AwsS3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClearAllCollectionsImplTest {

    @Mock
    private AwsS3Service awsS3Service;

    @Mock
    private DocumentCollectionRepository documentCollectionRepository;

    @InjectMocks
    private ClearAllCollectionsImpl clearAllCollectionsService;

    private String userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID().toString();
    }

    @Test
    void clearAllCollections_deletesFilesAndCollections() {
        // Arrange
        FileEntry file1 = FileEntry.builder().storageId("sid1").fileUrl("url1").build();
        FileEntry file2 = FileEntry.builder().storageId("sid2").fileUrl("url2").build();
        DocumentCollection collection1 = DocumentCollection.builder().files(new ArrayList<>(List.of(file1, file2))).build();

        FileEntry file3 = FileEntry.builder().storageId("sid3").fileUrl("url3").build();
        DocumentCollection collection2 = DocumentCollection.builder().files(new ArrayList<>(List.of(file3))).build();

        List<DocumentCollection> collections = List.of(collection1, collection2);

        when(documentCollectionRepository.findAllByUserId(userId)).thenReturn(collections);
        doNothing().when(awsS3Service).deleteFile(anyString());
        doNothing().when(documentCollectionRepository).deleteAllByUserId(userId);

        // Act
        clearAllCollectionsService.clearAllCollections(userId);

        // Assert
        verify(documentCollectionRepository).findAllByUserId(userId);
        verify(awsS3Service).deleteFile("url1");
        verify(awsS3Service).deleteFile("url2");
        verify(awsS3Service).deleteFile("url3");
        verify(documentCollectionRepository).deleteAllByUserId(userId);
        verifyNoMoreInteractions(awsS3Service, documentCollectionRepository);
    }

    @Test
    void clearAllCollections_handlesEmptyCollections() {
        // Arrange
        when(documentCollectionRepository.findAllByUserId(userId)).thenReturn(new ArrayList<>());
        doNothing().when(documentCollectionRepository).deleteAllByUserId(userId);

        // Act
        clearAllCollectionsService.clearAllCollections(userId);

        // Assert
        verify(documentCollectionRepository).findAllByUserId(userId);
        verify(documentCollectionRepository).deleteAllByUserId(userId);
        verifyNoInteractions(awsS3Service);
    }

    @Test
    void clearAllCollections_handlesCollectionsWithNoFiles() {
        // Arrange
        DocumentCollection collection1 = DocumentCollection.builder().files(new ArrayList<>()).build();
        DocumentCollection collection2 = DocumentCollection.builder().files(null).build();
        List<DocumentCollection> collections = List.of(collection1, collection2);

        when(documentCollectionRepository.findAllByUserId(userId)).thenReturn(collections);
        doNothing().when(documentCollectionRepository).deleteAllByUserId(userId);

        // Act
        clearAllCollectionsService.clearAllCollections(userId);

        // Assert
        verify(documentCollectionRepository).findAllByUserId(userId);
        verify(documentCollectionRepository).deleteAllByUserId(userId);
        verifyNoInteractions(awsS3Service);
    }

    @Test
    void clearAllCollections_handlesFilesWithNullStorageId() {
        // Arrange
        FileEntry fileWithStorageId = FileEntry.builder().storageId("sid1").fileUrl("url1").build();
        FileEntry fileWithoutStorageId = FileEntry.builder().storageId(null).fileUrl("url2").build();
        DocumentCollection collection = DocumentCollection.builder().files(new ArrayList<>(List.of(fileWithStorageId, fileWithoutStorageId))).build();

        when(documentCollectionRepository.findAllByUserId(userId)).thenReturn(List.of(collection));
        doNothing().when(awsS3Service).deleteFile("url1");
        doNothing().when(documentCollectionRepository).deleteAllByUserId(userId);

        // Act
        clearAllCollectionsService.clearAllCollections(userId);

        // Assert
        verify(documentCollectionRepository).findAllByUserId(userId);
        verify(awsS3Service).deleteFile("url1");
        verify(awsS3Service, never()).deleteFile("url2");
        verify(documentCollectionRepository).deleteAllByUserId(userId);
    }

    @Test
    void clearAllCollections_continuesWhenS3DeleteFails() {
        // Arrange
        FileEntry file1 = FileEntry.builder().storageId("sid1").fileUrl("url1").build();
        FileEntry file2 = FileEntry.builder().storageId("sid2").fileUrl("url2").build();
        DocumentCollection collection = DocumentCollection.builder().files(new ArrayList<>(List.of(file1, file2))).build();

        when(documentCollectionRepository.findAllByUserId(userId)).thenReturn(List.of(collection));
        doThrow(new RuntimeException("S3 is down")).when(awsS3Service).deleteFile("url1");
        doNothing().when(awsS3Service).deleteFile("url2");
        doNothing().when(documentCollectionRepository).deleteAllByUserId(userId);

        // Act
        clearAllCollectionsService.clearAllCollections(userId);

        // Assert
        verify(documentCollectionRepository).findAllByUserId(userId);
        verify(awsS3Service).deleteFile("url1");
        verify(awsS3Service).deleteFile("url2");
        verify(documentCollectionRepository).deleteAllByUserId(userId);
    }
}