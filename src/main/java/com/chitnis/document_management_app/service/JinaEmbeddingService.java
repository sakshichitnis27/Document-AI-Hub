package com.chitnis.document_management_app.service;
import com.chitnis.document_management_app.util.VectorUtils;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Embedding service using Jina AI's free embedding API.
 * Uses the jina-embeddings-v2-base-en model (768 dimensions).
 */
@Service
public class JinaEmbeddingService implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(JinaEmbeddingService.class);
    private static final String JINA_API_URL = "https://api.jina.ai/v1/embeddings";
    private static final String MODEL = "jina-embeddings-v2-base-en";
    private static final int DIMENSION = 768;

    private final RestTemplate restTemplate = new RestTemplate();
    private final String apiKey;

    public JinaEmbeddingService(@Value("${jina.api.key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @PostConstruct
    public void init() {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("⚠️  Jina API key not configured. Embeddings will use fallback mode.");
            log.warn("   Get a free API key at: https://jina.ai/embeddings/");
        } else {
            log.info("✅ Jina Embedding Service initialized with model: {}", MODEL);
        }
    }

    @Override
    public List<Double> embed(String text) {
        if (text == null || text.isBlank()) {
            return createZeroVector();
        }

        // Truncate very long texts to avoid API limits
        String truncated = text.length() > 8000 ? text.substring(0, 8000) : text;

        // If no API key, use simple fallback
        if (apiKey == null || apiKey.isBlank()) {
            return createSimpleEmbedding(truncated);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            Map<String, Object> body = Map.of(
                    "model", MODEL,
                    "input", List.of(truncated)
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            JinaEmbeddingResponse response = restTemplate.postForObject(
                    JINA_API_URL,
                    request,
                    JinaEmbeddingResponse.class
            );

            if (response != null && response.data != null && !response.data.isEmpty()) {
                List<Double> embedding = response.data.get(0).embedding;
                if (embedding != null && !embedding.isEmpty()) {
                    return embedding;
                }
            }

            log.warn("Empty response from Jina API, using fallback");
            return createSimpleEmbedding(truncated);

        } catch (RestClientException ex) {
            log.error("Failed to get embeddings from Jina API: {}", ex.getMessage());
            return createSimpleEmbedding(truncated);
        }
    }

    @Override
    public int getDimension() {
        return DIMENSION;
    }

    /**
     * Simple fallback embedding based on text statistics.
     * This is NOT a real embedding but allows the system to work without an API key.
     */
    private List<Double> createSimpleEmbedding(String text) {
        List<Double> embedding = new ArrayList<>(DIMENSION);
        String lower = text.toLowerCase();

        // Simple hash-based features spread across the vector
        for (int i = 0; i < DIMENSION; i++) {
            int hash = (text.hashCode() + i * 31) % 1000;
            double value = (hash / 1000.0) * 2.0 - 1.0; // Normalize to [-1, 1]

            // Add some text-based features
            if (i < 10) {
                // Character frequency features
                char c = (char) ('a' + i);
                long count = lower.chars().filter(ch -> ch == c).count();
                value += (count / (double) text.length()) * 0.1;
            } else if (i < 20) {
                // Word-based features
                value += text.split("\\s+").length / 100.0;
            }

            embedding.add(Math.max(-1.0, Math.min(1.0, value)));
        }

        return embedding;
    }

    private List<Double> createZeroVector() {
        List<Double> embedding = new ArrayList<>(DIMENSION);
        for (int i = 0; i < DIMENSION; i++) {
            embedding.add(0.0);
        }
        return embedding;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class JinaEmbeddingResponse {
        private List<EmbeddingData> data;

        public List<EmbeddingData> getData() {
            return data;
        }

        public void setData(List<EmbeddingData> data) {
            this.data = data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EmbeddingData {
        private List<Double> embedding;

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }
}
