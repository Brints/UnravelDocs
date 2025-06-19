package com.extractor.unraveldocs.documents.controller;

import com.extractor.unraveldocs.documents.dto.response.*;
import com.extractor.unraveldocs.documents.service.DocumentService;
import com.extractor.unraveldocs.exceptions.custom.BadRequestException;
import com.extractor.unraveldocs.exceptions.custom.ForbiddenException;
import com.extractor.unraveldocs.exceptions.custom.TooManyRequestsException;
import com.extractor.unraveldocs.user.model.User;
import com.extractor.unraveldocs.user.repository.UserRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Endpoints for document management including upload, retrieval, and deletion")
public class DocumentController {
    private final DocumentService documentService;
    private final UserRepository userRepository;
    private final Map<String, Bucket> rateLimitBuckets = new ConcurrentHashMap<>();

    private User getAuthenticatedUser(Authentication authenticatedUser) {
        if (authenticatedUser == null) {
            throw new ForbiddenException("You must be logged in to perform this action.");
        }
        String email = authenticatedUser.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ForbiddenException("User not found. Please log in again."));
    }

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
    public ResponseEntity<DocumentCollectionResponse<DocumentCollectionUploadData>> uploadDocuments(
            @Parameter(description = "Files to be uploaded", required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("files") MultipartFile[] files,
            Authentication authenticatedUser
    ) {
        User user = getAuthenticatedUser(authenticatedUser);

        // Rate limiting: Allow 10 uploads per minute per user
        Bucket bucket = this.rateLimitBuckets.computeIfAbsent(user.getId(), this::createRateLimitBucket);
        if (!bucket.tryConsume(1)) {
            throw new TooManyRequestsException("Rate limit exceeded. Please try again later.");
        }

        if (files == null || files.length == 0) {
            throw new BadRequestException("No files provided for upload.");
        }

        DocumentCollectionResponse<DocumentCollectionUploadData> response =
                documentService.uploadDocuments(files, user);

        return ResponseEntity.ok(response);
    }

    private Bucket createRateLimitBucket(String userId) {
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }

    @Operation(
            summary = "Get a specific document collection by its ID",
            description = "Retrieves details of a document collection, including all its files.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved document collection",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = GetDocumentCollectionData.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User not authorized or not logged in"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Document collection not found")
            }
    )
    @GetMapping("/{collectionId}")
    public ResponseEntity<DocumentCollectionResponse<GetDocumentCollectionData>> getDocumentCollectionById(
            @Parameter(description = "ID of the document collection") @PathVariable String collectionId,
            Authentication authenticatedUser
    ) {
        User user = getAuthenticatedUser(authenticatedUser);

        DocumentCollectionResponse<GetDocumentCollectionData> response =
                documentService.getDocumentCollectionById(collectionId, user.getId());

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get all document collections for the authenticated user",
            description = "Retrieves a list of all document collections created by the currently logged-in user.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved user's document collections",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = DocumentCollectionResponse.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User not logged in")
            }
    )
    @GetMapping("/my-collections")
    public ResponseEntity<DocumentCollectionResponse<List<DocumentCollectionSummary>>> getAllDocumentCollectionsForUser(
            Authentication authenticatedUser
    ) {
        User user = getAuthenticatedUser(authenticatedUser);

        DocumentCollectionResponse<List<DocumentCollectionSummary>> response =
                documentService.getAllDocumentCollectionsByUser(user.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get a specific file from a document collection",
            description = "Retrieves details of a single file within a specified document collection using its document ID.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successfully retrieved the file",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = FileEntryData.class))),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User not authorized or not logged in"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Collection or file not found")
            }
    )
    @GetMapping("/{collectionId}/files/{documentId}")
    public ResponseEntity<DocumentCollectionResponse<FileEntryData>> getFileFromCollection(
            @Parameter(description = "ID of the document collection") @PathVariable String collectionId,
            @Parameter(description = "Document ID of the file to retrieve") @PathVariable String documentId,
            Authentication authenticatedUser
    ) {
        User user = getAuthenticatedUser(authenticatedUser);

        DocumentCollectionResponse<FileEntryData> response =
                documentService.getFileFromCollection(collectionId, documentId, user.getId());
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
        User user = getAuthenticatedUser(authenticatedUser);
        documentService.deleteDocument(collectionId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Delete a specific file from a document collection",
            description = "Allows users to delete a single file from their uploaded document collection using its document ID.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully deleted the file from the collection"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User not logged in or not authorized"),
                    @ApiResponse(responseCode = "404", description = "Not Found - Collection or file not found")
            }
    )
    @DeleteMapping("/{collectionId}/files/{documentId}")
    public ResponseEntity<Void> deleteFileFromCollection(
            @Parameter(description = "ID of the document collection") @PathVariable String collectionId,
            @Parameter(description = "Document ID of the file to be deleted") @PathVariable String documentId,
            Authentication authenticatedUser
    ) {
        User user = getAuthenticatedUser(authenticatedUser);
        documentService.deleteFileFromCollection(collectionId, documentId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("clear-all")
    @Operation(
            summary = "Clear all document collections for the authenticated user",
            description = "Deletes all document collections and their files for the currently logged-in user.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Successfully cleared all document collections"),
                    @ApiResponse(responseCode = "403", description = "Forbidden - User not logged in")
            }
    )
    public ResponseEntity<Void> clearAllCollections(Authentication authenticatedUser) {
        User user = getAuthenticatedUser(authenticatedUser);
        documentService.clearAllCollections(user.getId());
        return ResponseEntity.noContent().build();
    }
}