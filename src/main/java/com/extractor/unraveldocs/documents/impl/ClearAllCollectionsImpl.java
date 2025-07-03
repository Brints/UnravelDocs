package com.extractor.unraveldocs.documents.impl;

import com.extractor.unraveldocs.documents.interfaces.ClearAllCollectionsService;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.utils.imageupload.aws.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClearAllCollectionsImpl implements ClearAllCollectionsService {
    private final AwsS3Service awsS3Service;
    private final DocumentCollectionRepository documentCollectionRepository;

    @Override
    @Transactional
    public void clearAllCollections(String userId) {
        // Fetch all collections for the user
        documentCollectionRepository.findAllByUserId(userId).forEach(collection -> {
            // Delete all files in the collection
            collection.getFiles().forEach(fileEntry -> {
                if (fileEntry.getStorageId() != null) {
                    try {
                        awsS3Service.deleteFile(fileEntry.getFileUrl());
                    } catch (Exception e) {
                        // Log the error but continue processing other files
                        System.err.println("Failed to delete file: " + e.getMessage());
                    }
                }
            });
        });

        // Delete all collections for the user
        documentCollectionRepository.deleteAllByUserId(userId);
    }
}
