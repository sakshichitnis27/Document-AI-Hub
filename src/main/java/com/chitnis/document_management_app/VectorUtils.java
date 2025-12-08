package com.chitnis.document_management_app;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for vector operations and text chunking.
 */
public class VectorUtils {

    /**
     * Split text into chunks of approximately the specified size.
     * Tries to break at sentence boundaries when possible.
     *
     * @param text The text to split
     * @param chunkSize Target size for each chunk (in characters)
     * @return List of text chunks
     */
    public static List<String> splitIntoChunks(String text, int chunkSize) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // Normalize whitespace
        String normalized = text.replaceAll("\\s+", " ").trim();

        int start = 0;
        while (start < normalized.length()) {
            int end = Math.min(start + chunkSize, normalized.length());

            // If not at the end, try to break at a sentence boundary
            if (end < normalized.length()) {
                int lastPeriod = normalized.lastIndexOf('.', end);
                int lastQuestion = normalized.lastIndexOf('?', end);
                int lastExclamation = normalized.lastIndexOf('!', end);
                int lastNewline = normalized.lastIndexOf('\n', end);

                int sentenceEnd = Math.max(
                        Math.max(lastPeriod, lastQuestion),
                        Math.max(lastExclamation, lastNewline)
                );

                // Only use sentence boundary if it's not too far back
                if (sentenceEnd > start + (chunkSize / 2)) {
                    end = sentenceEnd + 1;
                } else {
                    // Try to break at a space
                    int lastSpace = normalized.lastIndexOf(' ', end);
                    if (lastSpace > start) {
                        end = lastSpace;
                    }
                }
            }

            String chunk = normalized.substring(start, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            start = end;
        }

        return chunks;
    }

    /**
     * Calculate cosine similarity between two vectors.
     *
     * @param a First vector
     * @param b Second vector
     * @return Cosine similarity score (0 to 1, where 1 is most similar)
     */
    public static double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return 0.0;
        }

        if (a.size() != b.size()) {
            throw new IllegalArgumentException(
                    "Vectors must have same dimension: " + a.size() + " vs " + b.size()
            );
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.size(); i++) {
            double x = a.get(i);
            double y = b.get(i);
            dotProduct += x * y;
            normA += x * x;
            normB += y * y;
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    /**
     * Parse a JSON array string into a list of doubles.
     *
     * @param json JSON array string (e.g., "[0.1, 0.2, 0.3]")
     * @return List of doubles
     */
    public static List<Double> parseJsonToVector(String json) {
        List<Double> vector = new ArrayList<>();

        if (json == null || json.isEmpty()) {
            return vector;
        }

        // Remove brackets and whitespace
        String cleaned = json.trim()
                .replaceAll("^\\[", "")
                .replaceAll("\\]$", "")
                .trim();

        if (cleaned.isEmpty()) {
            return vector;
        }

        // Split by comma and parse each value
        String[] parts = cleaned.split(",");
        for (String part : parts) {
            try {
                vector.add(Double.parseDouble(part.trim()));
            } catch (NumberFormatException e) {
                // Skip invalid values
            }
        }

        return vector;
    }

    /**
     * Convert a list of doubles to a JSON array string.
     *
     * @param vector List of doubles
     * @return JSON array string
     */
    public static String vectorToJson(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return "[]";
        }

        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                json.append(",");
            }
            json.append(vector.get(i));
        }
        json.append("]");

        return json.toString();
    }
}
