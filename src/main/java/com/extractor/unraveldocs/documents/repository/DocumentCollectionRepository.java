package com.extractor.unraveldocs.documents.repository;

import com.extractor.unraveldocs.documents.model.DocumentCollection;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentCollectionRepository extends JpaRepository<DocumentCollection, String> {
}