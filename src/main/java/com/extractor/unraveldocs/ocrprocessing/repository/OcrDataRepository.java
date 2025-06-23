package com.extractor.unraveldocs.ocrprocessing.repository;

import com.extractor.unraveldocs.ocrprocessing.model.OcrData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OcrDataRepository extends JpaRepository<OcrData, String> {
    Optional<OcrData> findByDocumentId(String documentId);

    @Query("SELECT o FROM OcrData o WHERE o.documentId IN :documentIds")
    List<OcrData> findByDocumentIdIn(@Param("documentIds") List<String> documentIds);
}
