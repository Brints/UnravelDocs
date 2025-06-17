package com.extractor.unraveldocs.documents.controller;

import com.extractor.unraveldocs.documents.dto.response.DocumentCollectionResponse;
import com.extractor.unraveldocs.documents.service.DocumentService;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Endpoints for document management including upload and deletion")
public class DocumentController {
    private final DocumentService documentService;
    private final UserRepository userRepository;

    @Operation(
            summary = "Upload one or more documents as a collection",
            description = "Allows users to upload multiple documents. These will be grouped as a single collection.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully processed document upload request",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DocumentCollectionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Bad Request - No files provided or invalid file(s)"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User not logged in or not found")
            }
    )
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentCollectionResponse> uploadDocuments(
            @Parameter(description = "Files to be uploaded", required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("files") MultipartFile[] files,
            Authentication authenticatedUser
    ) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("You must be logged in to upload documents");
        }

        if (files == null || files.length == 0) {
            throw new BadRequestException("No files provided for upload.");
        }


        String email = authenticatedUser.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("User not found"));

        DocumentCollectionResponse response = documentService.uploadDocuments(files, user);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Delete a document collection",
            description = "Allows users to delete their uploaded document collections.")
    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteDocument(
            @PathVariable String collectionId,
            Authentication authenticatedUser
    ) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("You must be logged in to delete documents");
        }

        String email = authenticatedUser.getName();
        String userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("User not found"))
                .getId();

        documentService.deleteDocument(collectionId, userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete a specific file from a document collection",
            description = "Allows users to delete a single file from their uploaded document collection using its storage ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully deleted the file from the collection"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User not logged in or not authorized"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Collection or file not found")
            }
    )
    @DeleteMapping("/{collectionId}/files/{documentId}")
    public ResponseEntity<Void> deleteFileFromCollection(
            @Parameter(description = "ID of the document collection") @PathVariable String collectionId,
            @Parameter(description = "Storage ID of the file to be deleted") @PathVariable String documentId,
            Authentication authenticatedUser
    ) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("You must be logged in to delete files.");
        }

        String email = authenticatedUser.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("User not found"));

        documentService.deleteFileFromCollection(collectionId, documentId, user.getId());
        return ResponseEntity.noContent().build();
    }
}