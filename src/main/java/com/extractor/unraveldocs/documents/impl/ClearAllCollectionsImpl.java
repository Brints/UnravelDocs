package com.extractor.unraveldocs.documents.impl;

import com.extractor.unraveldocs.documents.interfaces.ClearAllCollectionsService;
import com.extractor.unraveldocs.documents.model.FileEntry;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import com.extractor.unraveldocs.utils.imageupload.aws.AwsS3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ClearAllCollectionsImpl implements ClearAllCollectionsService {
    private final AwsS3Service awsS3Service;
    private final DocumentCollectionRepository documentCollectionRepository;

    @Override
    @Transactional
    public void clearAllCollections(String userId) {
        documentCollectionRepository.findAllByUserId(userId)
                .stream()
                .flatMap(collection -> collection.getFiles() != null ? collection.getFiles().stream() : Stream.empty())
                .filter(fileEntry -> fileEntry.getStorageId() != null && fileEntry.getFileUrl() != null)
                .forEach(this::deleteFileSafely);

        documentCollectionRepository.deleteAllByUserId(userId);
    }

    private void deleteFileSafely(FileEntry fileEntry) {
        try {
            awsS3Service.deleteFile(fileEntry.getFileUrl());
        } catch (Exception e) {
            log.error("Failed to delete file with URL {}: {}", fileEntry.getFileUrl(), e.getMessage());
        }
    }
}
