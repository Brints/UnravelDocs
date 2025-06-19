package com.extractor.unraveldocs.documents.service.impl;

import com.extractor.unraveldocs.documents.interfaces.ClearAllCollectionsService;
import com.extractor.unraveldocs.documents.repository.DocumentCollectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClearAllCollectionsImpl implements ClearAllCollectionsService {
    private final DocumentCollectionRepository documentCollectionRepository;

    @Override
    @Transactional
    public void clearAllCollections(String userId) {
        documentCollectionRepository.deleteAllByUserId(userId);
    }
}
