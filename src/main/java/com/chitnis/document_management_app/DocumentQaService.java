package com.chitnis.document_management_app;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DocumentQaService {

    private final DocumentRepository documentRepository;
    private final AiClient aiClient;

    public DocumentQaService(DocumentRepository documentRepository, AiClient aiClient) {
        this.documentRepository = documentRepository;
        this.aiClient = aiClient;
    }

    public DocumentQaResponse answerQuestion(Long documentId, String question) {
        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException("Question must not be empty.");
        }

        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new EntityNotFoundException("Document not found: " + documentId));

        if (document.getRawText() == null || document.getRawText().isBlank()) {
            throw new IllegalStateException("Text has not been extracted for document " + documentId);
        }

        String answer = aiClient.answerQuestion(document.getRawText(), question);
        String snippet = buildSnippet(document.getRawText(), question);
        return new DocumentQaResponse(documentId, question, answer, snippet);
    }

    private String buildSnippet(String rawText, String question) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }
        String normalized = rawText.replaceAll("\\s+", " ");
        String lowerText = normalized.toLowerCase();
        String[] tokens = question.toLowerCase().split("\\s+");

        int matchIndex = -1;
        for (String token : tokens) {
            if (token.length() < 3) {
                continue;
            }
            matchIndex = lowerText.indexOf(token);
            if (matchIndex != -1) {
                break;
            }
        }

        if (matchIndex == -1) {
            matchIndex = 0;
        }

        int start = Math.max(0, matchIndex - 120);
        int end = Math.min(normalized.length(), matchIndex + 120);
        String snippet = normalized.substring(start, end);
        if (start > 0) {
            snippet = "…" + snippet;
        }
        if (end < normalized.length()) {
            snippet = snippet + "…";
        }
        return snippet;
    }
}
