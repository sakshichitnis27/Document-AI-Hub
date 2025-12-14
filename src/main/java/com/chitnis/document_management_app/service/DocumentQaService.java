package com.chitnis.document_management_app.service;
import com.chitnis.document_management_app.ai.AiClient;
import com.chitnis.document_management_app.util.VectorUtils;
import com.chitnis.document_management_app.dto.DocumentQaResponse;
import com.chitnis.document_management_app.repository.DocumentChunkRepository;
import com.chitnis.document_management_app.repository.DocumentRepository;
import com.chitnis.document_management_app.entity.DocumentChunk;
import com.chitnis.document_management_app.entity.Document;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentQaService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final EmbeddingService embeddingService;
    private final AiClient aiClient;

    public DocumentQaService(DocumentRepository documentRepository,
                             DocumentChunkRepository documentChunkRepository,
                             EmbeddingService embeddingService,
                             AiClient aiClient) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.embeddingService = embeddingService;
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

        // Use vector search to find relevant chunks
        List<DocumentChunk> chunks = documentChunkRepository.findByDocumentIdOrderByChunkIndexAsc(documentId);

        if (chunks.isEmpty()) {
            // Fallback to full text if no chunks available
            String answer = aiClient.answerQuestion(document.getRawText(), question);
            String snippet = buildSnippet(document.getRawText(), question);
            return new DocumentQaResponse(documentId, question, answer, snippet);
        }

        // 1. Embed the question
        List<Double> questionVector = embeddingService.embed(question);

        // 2. Compute similarity for each chunk
        List<ScoredChunk> scoredChunks = new java.util.ArrayList<>();
        for (DocumentChunk chunk : chunks) {
            List<Double> chunkVector = VectorUtils.parseJsonToVector(chunk.getEmbedding());
            double score = VectorUtils.cosineSimilarity(questionVector, chunkVector);
            scoredChunks.add(new ScoredChunk(chunk, score));
        }

        // 3. Sort by similarity score (highest first)
        scoredChunks.sort((a, b) -> Double.compare(b.getScore(), a.getScore()));

        // 4. Take top 5 chunks
        List<DocumentChunk> topChunks = scoredChunks.stream()
                .limit(5)
                .map(ScoredChunk::getChunk)
                .toList();

        // 5. Build context from top chunks
        StringBuilder context = new StringBuilder();
        for (DocumentChunk chunk : topChunks) {
            context.append(chunk.getText()).append("\n\n");
        }

        // 6. Call AI with context + question
        String answer = aiClient.answerQuestion(context.toString(), question);
        String snippet = buildSnippetFromChunks(topChunks);

        return new DocumentQaResponse(documentId, question, answer, snippet);
    }

    private String buildSnippetFromChunks(List<DocumentChunk> chunks) {
        if (chunks.isEmpty()) {
            return "";
        }
        // Return the most relevant chunk (first one) as snippet
        String text = chunks.get(0).getText();
        if (text.length() > 300) {
            return text.substring(0, 300) + "…";
        }
        return text;
    }

    // Helper class to pair chunks with their similarity scores
    private static class ScoredChunk {
        private final DocumentChunk chunk;
        private final double score;

        public ScoredChunk(DocumentChunk chunk, double score) {
            this.chunk = chunk;
            this.score = score;
        }

        public DocumentChunk getChunk() {
            return chunk;
        }

        public double getScore() {
            return score;
        }
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
