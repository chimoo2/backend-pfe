package com.example.career.repository;

import com.example.career.model.Document;
import com.example.career.dto.DocumentDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    @Query("""
        SELECT new com.example.career.dto.DocumentDTO(
            d.id,
            d.fileName,
            d.originalFileName,
            d.filePath,
            d.fileSize,
            d.mimeType,
            d.documentType,
            d.uploadedAt,
            d.updatedAt
        )
        FROM Document d
        WHERE d.user.id = :userId
    """)
    List<DocumentDTO> findDocumentDTOsByUserId(Long userId);

    List<Document> findByUserIdAndDocumentType(Long userId, Document.DocumentType type);

    Optional<Document> findByIdAndUserId(Long id, Long userId);
}