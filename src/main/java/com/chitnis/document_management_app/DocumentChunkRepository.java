package com.chitnis.document_management_app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    List<DocumentChunk> findByDocumentIdOrderByChunkIndexAsc(Long documentId);

    @Modifying
    @Query("DELETE FROM DocumentChunk dc WHERE dc.documentId = :documentId")
    void deleteByDocumentId(@Param("documentId") Long documentId);

    long countByDocumentId(Long documentId);
}
