package com.chitnis.document_management_app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class MultiDocumentQaRequest {

    @NotEmpty(message = "At least one document ID must be provided.")
    private List<Long> documentIds;

    @NotBlank(message = "Question must not be empty.")
    private String question;

    public List<Long> getDocumentIds() {
        return documentIds;
    }

    public void setDocumentIds(List<Long> documentIds) {
        this.documentIds = documentIds;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
