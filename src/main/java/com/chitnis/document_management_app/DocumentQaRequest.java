package com.chitnis.document_management_app;

import jakarta.validation.constraints.NotBlank;

public class DocumentQaRequest {

    @NotBlank(message = "Question must not be empty.")
    private String question;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }
}
