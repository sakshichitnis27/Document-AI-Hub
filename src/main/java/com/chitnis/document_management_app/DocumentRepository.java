package com.chitnis.document_management_app;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserId(Long userId);

    @Query(value = """
            SELECT * FROM documents d
            WHERE d.user_id = :userId
            AND LOWER(d.raw_text) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY d.uploaded_at DESC
            LIMIT 10
            """, nativeQuery = true)
    List<Document> searchByRawText(@Param("userId") Long userId, @Param("query") String query);
}
