package com.chitnis.document_management_app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    @Query(value = """
            SELECT * FROM documents d
            WHERE LOWER(d.raw_text) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY d.uploaded_at DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Document> searchByRawText(@Param("query") String query);
}
