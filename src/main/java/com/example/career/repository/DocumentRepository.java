package com.example.career.repository;

import com.example.career.model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUserId(Long userId);
    List<Document> findByUserIdAndDocumentType(Long userId, Document.DocumentType type);
    Optional<Document> findByIdAndUserId(Long id, Long userId);
}
