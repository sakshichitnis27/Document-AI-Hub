package com.chitnis.document_management_app.controller;
import com.chitnis.document_management_app.service.DocumentQaService;
import com.chitnis.document_management_app.service.DocumentSummaryService;
import com.chitnis.document_management_app.service.DocumentService;
import com.chitnis.document_management_app.entity.DocumentSummary;
import com.chitnis.document_management_app.entity.Document;
import com.chitnis.document_management_app.dto.DocumentSearchResult;
import com.chitnis.document_management_app.dto.DocumentQaResponse;
import com.chitnis.document_management_app.dto.DocumentQaRequest;
import com.chitnis.document_management_app.dto.DocumentResponse;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentSummaryService documentSummaryService;
    private final DocumentQaService documentQaService;

    public DocumentController(DocumentService documentService,
                              DocumentSummaryService documentSummaryService,
                              DocumentQaService documentQaService) {
        this.documentService = documentService;
        this.documentSummaryService = documentSummaryService;
        this.documentQaService = documentQaService;
    }

    @PostMapping
    public ResponseEntity<?> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            Document saved = documentService.uploadDocument(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to store the uploaded file."));
        }
    }

    @GetMapping
    public List<DocumentResponse> listDocuments() {
        return documentService.getAllDocuments().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchDocuments(@RequestParam("query") String query) {
        try {
            return ResponseEntity.ok(documentService.searchDocuments(query));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/extract-text")
    public ResponseEntity<?> extractText(@PathVariable("id") Long documentId) {
        try {
            Document updated = documentService.extractText(documentId);
            int textLength = updated.getRawText() != null ? updated.getRawText().length() : 0;
            return ResponseEntity.ok(Map.of(
                    "documentId", updated.getId(),
                    "status", updated.getStatus().name(),
                    "textLength", textLength
            ));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unable to extract text from the document."));
        }
    }

    @GetMapping("/{id}/text")
    public ResponseEntity<?> getDocumentText(@PathVariable("id") Long documentId) {
        try {
            String text = documentService.getDocumentText(documentId);
            return ResponseEntity.ok(Map.of(
                    "documentId", documentId,
                    "rawText", text
            ));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/summarize")
    public ResponseEntity<?> summarizeDocument(@PathVariable("id") Long documentId) {
        try {
            DocumentSummary summary = documentSummaryService.summarizeDocument(documentId);
            return ResponseEntity.ok(Map.of(
                    "documentId", documentId,
                    "summaryId", summary.getId(),
                    "summaryText", summary.getSummaryText(),
                    "createdAt", summary.getCreatedAt()
            ));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/{id}/summary")
    public ResponseEntity<?> getLatestSummary(@PathVariable("id") Long documentId) {
        try {
            DocumentSummary summary = documentSummaryService.getLatestSummary(documentId);
            return ResponseEntity.ok(Map.of(
                    "documentId", documentId,
                    "summaryId", summary.getId(),
                    "summaryText", summary.getSummaryText(),
                    "createdAt", summary.getCreatedAt()
            ));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/qa")
    public ResponseEntity<?> answerQuestion(@PathVariable("id") Long documentId,
                                            @Valid @RequestBody DocumentQaRequest request) {
        try {
            DocumentQaResponse response = documentQaService.answerQuestion(documentId, request.getQuestion());
            return ResponseEntity.ok(Map.of(
                    "documentId", response.getDocumentId(),
                    "question", response.getQuestion(),
                    "answer", response.getAnswer(),
                    "sourceSnippet", response.getSourceSnippet()
            ));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/{id}/embeddings")
    public ResponseEntity<?> createEmbeddings(@PathVariable("id") Long documentId) {
        try {
            documentService.createEmbeddings(documentId);
            long chunkCount = documentService.getChunkCount(documentId);
            return ResponseEntity.ok(Map.of(
                    "documentId", documentId,
                    "chunkCount", chunkCount,
                    "message", "Embeddings created successfully"
            ));
        } catch (Exception ex) {
            ex.printStackTrace(); // Print full stack trace to console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", ex.getMessage(), "type", ex.getClass().getName()));
        }
    }

    @GetMapping("/{id}/chunks")
    public ResponseEntity<?> getChunkInfo(@PathVariable("id") Long documentId) {
        try {
            long chunkCount = documentService.getChunkCount(documentId);
            return ResponseEntity.ok(Map.of(
                    "documentId", documentId,
                    "chunkCount", chunkCount
            ));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
        }
    }

    private DocumentResponse toResponse(Document document) {
        return new DocumentResponse(
                document.getId(),
                document.getOriginalFileName(),
                document.getUploadedAt(),
                document.getStatus().name()
        );
    }
}
