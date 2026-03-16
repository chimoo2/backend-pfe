package com.example.career.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import com.example.career.model.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDTO {

    private Long id;
    private String fileName;
    private String originalFileName;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private Document.DocumentType documentType;
    private LocalDateTime uploadedAt;
    private LocalDateTime updatedAt;
}