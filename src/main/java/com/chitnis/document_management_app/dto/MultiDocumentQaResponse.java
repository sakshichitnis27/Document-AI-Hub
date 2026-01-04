package com.chitnis.document_management_app.dto;

import java.util.List;

public class MultiDocumentQaResponse {

    private final List<Long> documentIds;
    private final List<String> documentNames;
    private final String question;
    private final String answer;
    private final String sourceSnippet;

    public MultiDocumentQaResponse(List<Long> documentIds, List<String> documentNames, String question, String answer, String sourceSnippet) {
        this.documentIds = documentIds;
        this.documentNames = documentNames;
        this.question = question;
        this.answer = answer;
        this.sourceSnippet = sourceSnippet;
    }

    public List<Long> getDocumentIds() {
        return documentIds;
    }

    public List<String> getDocumentNames() {
        return documentNames;
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
