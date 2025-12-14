package com.chitnis.document_management_app.dto;

public class DocumentQaResponse {

    private final Long documentId;
    private final String question;
    private final String answer;
    private final String sourceSnippet;

    public DocumentQaResponse(Long documentId, String question, String answer, String sourceSnippet) {
        this.documentId = documentId;
        this.question = question;
        this.answer = answer;
        this.sourceSnippet = sourceSnippet;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    public String getSourceSnippet() {
        return sourceSnippet;
    }
}
