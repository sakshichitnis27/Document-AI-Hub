package com.chitnis.document_management_app.repository;
import com.chitnis.document_management_app.entity.Document;
import com.chitnis.document_management_app.entity.DocumentSummary;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentSummaryRepository extends JpaRepository<DocumentSummary, Long> {
    Optional<DocumentSummary> findTopByDocumentOrderByCreatedAtDesc(Document document);
}
