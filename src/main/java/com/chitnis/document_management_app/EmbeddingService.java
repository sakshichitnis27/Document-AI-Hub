package com.chitnis.document_management_app;

import java.util.List;

/**
 * Service for generating text embeddings (vector representations).
 */
public interface EmbeddingService {

    /**
     * Generate an embedding vector for the given text.
     *
     * @param text The input text to embed
     * @return A list of doubles representing the embedding vector
     */
    List<Double> embed(String text);

    /**
     * Get the dimension of the embedding vectors produced by this service.
     *
     * @return The embedding dimension
     */
    int getDimension();
}
