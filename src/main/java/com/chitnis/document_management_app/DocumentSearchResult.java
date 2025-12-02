package com.chitnis.document_management_app;

public class DocumentSearchResult {

    private final Long documentId;
    private final String originalFileName;
    private final String snippet;

    public DocumentSearchResult(Long documentId, String originalFileName, String snippet) {
        this.documentId = documentId;
        this.originalFileName = originalFileName;
        this.snippet = snippet;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getOriginalFileName() {
        return originalFileName;
    }

    public String getSnippet() {
        return snippet;
    }
}
