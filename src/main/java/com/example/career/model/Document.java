package com.example.career.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    private String fileName;              // Nom stocké sur disque (5_1707427200000.pdf)
    private String originalFileName;      // Nom original (Lettre d'Affectation.pdf)
    private String filePath;              // Chemin relatif (/uploads/cvs/5_1707427200000.pdf)
    private Long fileSize;                // Taille en bytes
    private String mimeType;              // application/pdf, etc.

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;    // CV, COVER_LETTER, etc.

    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum DocumentType {
        CV, COVER_LETTER, CERTIFICATE, OTHER
    }
}
