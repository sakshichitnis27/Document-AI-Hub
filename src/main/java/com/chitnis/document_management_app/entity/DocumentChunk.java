package com.chitnis.document_management_app.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "document_chunk", indexes = {
    @Index(name = "idx_document_id", columnList = "document_id")
})
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(columnDefinition = "JSONB", nullable = false)
    private String embedding;

    @Column(name = "created_at")
    private Instant createdAt;

    public DocumentChunk() {
    }

    public DocumentChunk(Long documentId, Integer chunkIndex, String text, String embedding) {
        this.documentId = documentId;
        this.chunkIndex = chunkIndex;
        this.text = text;
        this.embedding = embedding;
        this.createdAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public Integer getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(Integer chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
