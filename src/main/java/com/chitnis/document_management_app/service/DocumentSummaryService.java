package com.chitnis.document_management_app.service;
import com.chitnis.document_management_app.ai.AiClient;
import com.chitnis.document_management_app.repository.DocumentSummaryRepository;
import com.chitnis.document_management_app.repository.DocumentRepository;
import com.chitnis.document_management_app.entity.DocumentSummary;
import com.chitnis.document_management_app.entity.Document;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class DocumentSummaryService {

    private final DocumentRepository documentRepository;
    private final DocumentSummaryRepository documentSummaryRepository;
    private final AiClient aiClient;

    public DocumentSummaryService(DocumentRepository documentRepository,
                                  DocumentSummaryRepository documentSummaryRepository,
                                  AiClient aiClient) {
        this.documentRepository = documentRepository;
        this.documentSummaryRepository = documentSummaryRepository;
        this.aiClient = aiClient;
    }

    @Transactional
    public DocumentSummary summarizeDocument(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));

        if (document.getRawText() == null || document.getRawText().isBlank()) {
            throw new IllegalStateException("Text has not been extracted for document " + documentId);
        }

        String summaryText = aiClient.summarize(document.getRawText());

        DocumentSummary summary = new DocumentSummary();
        summary.setDocument(document);
        summary.setSummaryText(summaryText);
        summary.setCreatedAt(Instant.now());

        return documentSummaryRepository.save(summary);
    }

    public DocumentSummary getLatestSummary(Long documentId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));

        return documentSummaryRepository.findTopByDocumentOrderByCreatedAtDesc(document)
                .orElseThrow(() -> new EntityNotFoundException("No summary found for document " + documentId));
    }
}
