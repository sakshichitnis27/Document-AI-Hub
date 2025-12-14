package com.chitnis.document_management_app.dto;

import java.time.Instant;

public class DocumentResponse {

    private Long id;
    private String originalFileName;
    private Instant uploadedAt;
    private String status;

    public DocumentResponse(Long id, String originalFileName, Instant uploadedAt, String status) {
        this.id = id;
        this.originalFileName = originalFileName;
        this.uploadedAt = uploadedAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public String getStatus() {
        return status;
    }
}
