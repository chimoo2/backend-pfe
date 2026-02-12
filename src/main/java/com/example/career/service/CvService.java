package com.example.career.service;

import com.example.career.model.Document;
import com.example.career.model.User;
import com.example.career.repository.DocumentRepository;
import com.example.career.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Service
public class CvService {

    @Value("${cv.upload.dir:uploads/cvs/}")
    private String uploadDir;
    
    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    public CvService(DocumentRepository documentRepository, UserRepository userRepository) {
        this.documentRepository = documentRepository;
        this.userRepository = userRepository;
    }

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    public Document saveCv(MultipartFile file, Long userId) throws IOException {
        // Validation
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Fichier vide");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Fichier trop volumineux. Max 10MB");
        }

        if (!ALLOWED_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Type de fichier non autorisé. Accepté: PDF, DOC, DOCX");
        }

        // Vérifier que l'utilisateur existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        // Créer le répertoire
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Générer le nom du fichier
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = userId + "_" + System.currentTimeMillis() + extension;
        Path filePath = Paths.get(uploadDir).resolve(fileName);

        // Sauvegarder le fichier sur disque
        Files.write(filePath, file.getBytes());
        String filePathUrl = "/uploads/cvs/" + fileName;

        // Supprimer l'ancien CV s'il existe
        documentRepository.findByUserIdAndDocumentType(userId, Document.DocumentType.CV)
                .forEach(oldDoc -> {
                    try {
                        this.deleteDocument(oldDoc.getId(), userId);
                    } catch (IOException ignored) {}
                });

        // Créer l'entité Document
        Document doc = new Document();
        doc.setUser(user);
        doc.setFileName(fileName);
        doc.setOriginalFileName(originalFileName);
        doc.setFilePath(filePathUrl);
        doc.setFileSize(file.getSize());
        doc.setMimeType(file.getContentType());
        doc.setDocumentType(Document.DocumentType.CV);

        return documentRepository.save(doc);
    }

    public void deleteDocument(Long docId, Long userId) throws IOException {
        // Vérifier que le document appartient à l'utilisateur (SÉCURITÉ!)
        Document doc = documentRepository.findByIdAndUserId(docId, userId)
                .orElseThrow(() -> new RuntimeException("Document non trouvé ou accès refusé"));

        String fileName = doc.getFileName();
        Path filePath = Paths.get(uploadDir).resolve(fileName);
        
        // Supprimer le fichier
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }

        // Supprimer le document en BDD
        documentRepository.delete(doc);
    }

    public void deleteCv(String cvPath) throws IOException {
        if (cvPath != null && !cvPath.isEmpty()) {
            String fileName = cvPath.substring(cvPath.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadDir).resolve(fileName);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }
    }
}
