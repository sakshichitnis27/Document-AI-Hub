package com.chitnis.document_management_app.service;
import com.chitnis.document_management_app.util.VectorUtils;
import com.chitnis.document_management_app.dto.DocumentSearchResult;
import com.chitnis.document_management_app.repository.DocumentChunkRepository;
import com.chitnis.document_management_app.repository.DocumentRepository;
import com.chitnis.document_management_app.entity.User;
import com.chitnis.document_management_app.entity.DocumentChunk;
import com.chitnis.document_management_app.entity.DocumentStatus;
import com.chitnis.document_management_app.entity.Document;

import jakarta.persistence.EntityNotFoundException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;

    // value from application.properties
    @Value("${app.upload-dir}")
    private String uploadDir;

    @Value("${app.chunk-size:800}")
    private int chunkSize;

    public DocumentService(DocumentRepository documentRepository,
                           DocumentChunkRepository documentChunkRepository,
                           EmbeddingService embeddingService) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingService = embeddingService;
    }

    public Document uploadDocument(MultipartFile file) throws IOException {
        validateFile(file);

        // 1. Ensure upload directory exists
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 2. Generate unique file name
        String originalFileName = file.getOriginalFilename();
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        String storedFileName = UUID.randomUUID() + extension;
        Path targetPath = uploadPath.resolve(storedFileName);

        // 3. Save file to disk
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 4. Save metadata in DB
        Document doc = new Document();
        doc.setOriginalFileName(originalFileName);
        doc.setStoredFilePath(targetPath.toString());
        doc.setMimeType(file.getContentType());
        doc.setSizeInBytes(file.getSize());
        doc.setUploadedAt(Instant.now());
        doc.setStatus(DocumentStatus.UPLOADED);
        doc.setWorkspaceId(1L); // Default workspace
        doc.setUserId(getCurrentUserId()); // Associate with current user

        return documentRepository.save(doc);
    }

    public List<Document> getAllDocuments() {
        Long userId = getCurrentUserId();
        return documentRepository.findByUserId(userId);
    }

    public List<DocumentSearchResult> searchDocuments(String query) {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("Search query must not be empty.");
        }

        Long userId = getCurrentUserId();
        List<Document> matches = documentRepository.searchByRawText(userId, query);
        return matches.stream()
                .map(doc -> new DocumentSearchResult(
                        doc.getId(),
                        doc.getOriginalFileName(),
                        buildSnippet(doc.getRawText(), query)
                ))
                .toList();
    }

    public Document extractText(Long documentId) throws IOException {
        Document document = findDocument(documentId);

        Path filePath = Paths.get(document.getStoredFilePath());
        if (!Files.exists(filePath)) {
            throw new IllegalStateException("Stored file is missing on disk for document " + documentId);
        }

        try (PDDocument pdfDocument = Loader.loadPDF(filePath.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(pdfDocument);
            document.setRawText(text);
            document.setStatus(DocumentStatus.TEXT_EXTRACTED);
            Document saved = documentRepository.save(document);

            // Try to create embeddings, but don't fail if it errors
            try {
                createEmbeddings(saved.getId());
            } catch (Exception e) {
                // Log error but don't fail the extraction
                System.err.println("Warning: Failed to create embeddings: " + e.getMessage());
            }

            return saved;
        }
    }

    /**
     * Create embeddings for a document by chunking the text and embedding each chunk.
     */
    @Transactional
    public void createEmbeddings(Long documentId) {
        Document document = findDocument(documentId);
        if (document.getRawText() == null || document.getRawText().isBlank()) {
            throw new IllegalStateException("Text has not been extracted for document " + documentId);
        }
        createEmbeddingsInternal(document);
    }

    private void createEmbeddingsInternal(Document document) {
        String text = document.getRawText();
        if (text == null || text.isBlank()) {
            return;
        }

        // Delete existing chunks if any
        documentChunkRepository.deleteByDocumentId(document.getId());

        // Split text into chunks
        List<String> chunks = VectorUtils.splitIntoChunks(text, chunkSize);

        // Create embeddings for each chunk
        int index = 0;
        for (String chunkText : chunks) {
            if (chunkText.isBlank()) {
                continue;
            }

            // Get embedding vector
            List<Double> embedding = embeddingService.embed(chunkText);

            // Convert to JSON
            String embeddingJson = VectorUtils.vectorToJson(embedding);

            // Save chunk
            DocumentChunk chunk = new DocumentChunk(
                    document.getId(),
                    index++,
                    chunkText,
                    embeddingJson
            );
            documentChunkRepository.save(chunk);
        }
    }

    public long getChunkCount(Long documentId) {
        return documentChunkRepository.countByDocumentId(documentId);
    }

    public String getDocumentText(Long documentId) {
        Document document = findDocument(documentId);
        if (document.getRawText() == null) {
            throw new IllegalStateException("Text has not been extracted for document " + documentId);
        }
        return document.getRawText();
    }

    public Path getDocumentFile(Long documentId) {
        Document document = findDocument(documentId);
        Path filePath = Paths.get(document.getStoredFilePath());
        if (!Files.exists(filePath)) {
            throw new IllegalStateException("Stored file is missing on disk for document " + documentId);
        }
        return filePath;
    }

    public Document getDocumentById(Long documentId) {
        return findDocument(documentId);
    }

    private Document findDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));

        // Verify that the document belongs to the current user
        Long currentUserId = getCurrentUserId();
        if (!document.getUserId().equals(currentUserId)) {
            throw new EntityNotFoundException("Document not found: " + documentId);
        }

        return document;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated");
        }
        User user = (User) authentication.getPrincipal();
        return user.getId();
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file must not be empty.");
        }

        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename();

        boolean hasPdfMimeType = contentType != null && contentType.equalsIgnoreCase("application/pdf");
        boolean hasPdfExtension = originalName != null
                && originalName.toLowerCase(Locale.ENGLISH).endsWith(".pdf");

        if (!hasPdfMimeType && !hasPdfExtension) {
            throw new IllegalArgumentException("Only PDF files are supported.");
        }
    }

    private String buildSnippet(String rawText, String query) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }
        String normalizedText = rawText.replaceAll("\\s+", " ");
        String lowerText = normalizedText.toLowerCase();
        String lowerQuery = query.toLowerCase();
        int matchIndex = lowerText.indexOf(lowerQuery);

        if (matchIndex == -1) {
            return normalizedText.substring(0, Math.min(200, normalizedText.length()));
        }

        int start = Math.max(0, matchIndex - 100);
        int end = Math.min(normalizedText.length(), matchIndex + lowerQuery.length() + 100);
        String snippet = normalizedText.substring(start, end);

        if (start > 0) {
            snippet = "…" + snippet;
        }
        if (end < normalizedText.length()) {
            snippet = snippet + "…";
        }
        return snippet;
    }
}
