package com.chitnis.document_management_app;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentSummaryRepository extends JpaRepository<DocumentSummary, Long> {
    Optional<DocumentSummary> findTopByDocumentOrderByCreatedAtDesc(Document document);
}
