package com.extractor.unraveldocs.documents.interfaces;

import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionUploadData;
import com.extractor.unraveldocs.user.model.User;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentUploadService {
    DocumentCollectionResponse<DocumentCollectionUploadData> uploadDocuments(MultipartFile[] files, User user);
}