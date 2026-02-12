package com.example.career.controller;

import com.example.career.model.Document;
import com.example.career.repository.DocumentRepository;
import com.example.career.security.JwtService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/documents")
@CrossOrigin(origins = "*")
public class DocumentController {

    private final DocumentRepository documentRepository;
    private final JwtService jwtService;
    private static final String UPLOAD_DIR = "uploads/cvs/";

    public DocumentController(DocumentRepository documentRepository, JwtService jwtService) {
        this.documentRepository = documentRepository;
        this.jwtService = jwtService;
    }

    /**
     * Endpoint sécurisé pour télécharger un document
     * L'utilisateur ne peut télécharger que ses propres documents
     */
    @GetMapping("/{docId}/download")
    public ResponseEntity<?> downloadDocument(
            @PathVariable Long docId,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        
        // Vérifier le token JWT
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        }

        String token = authHeader.substring(7);
        String email = jwtService.validateTokenAndGetSubject(token);
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalide"));
        }

        // Récupérer le document
        Optional<Document> docOpt = documentRepository.findById(docId);
        if (!docOpt.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("error", "Document non trouvé"));
        }

        Document doc = docOpt.get();

        // Vérifier que le document appartient à l'utilisateur connecté
        if (!doc.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès refusé"));
        }

        // Charger le fichier
        Path filePath = Paths.get(UPLOAD_DIR).resolve(doc.getFileName());
        if (!Files.exists(filePath)) {
            return ResponseEntity.status(404).body(Map.of("error", "Fichier non trouvé"));
        }

        try {
            Resource resource = new FileSystemResource(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getMimeType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + doc.getOriginalFileName() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors du téléchargement"));
        }
    }

    /**
     * Récupérer tous les documents de l'utilisateur
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserDocuments(
            @PathVariable Long userId,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        
        // Vérifier le token JWT
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        }

        String token = authHeader.substring(7);
        String email = jwtService.validateTokenAndGetSubject(token);
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalide"));
        }

        // Récupérer les documents
        List<Document> documents = documentRepository.findByUserId(userId);
        return ResponseEntity.ok(documents);
    }

    /**
     * Supprimer un document (sécurisé)
     */
    @DeleteMapping("/{docId}")
    public ResponseEntity<?> deleteDocument(
            @PathVariable Long docId,
            @RequestHeader(name = "Authorization", required = false) String authHeader) {
        
        // Vérifier le token JWT
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
        }

        String token = authHeader.substring(7);
        String email = jwtService.validateTokenAndGetSubject(token);
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalide"));
        }

        // Récupérer le document
        Optional<Document> docOpt = documentRepository.findById(docId);
        if (!docOpt.isPresent()) {
            return ResponseEntity.status(404).body(Map.of("error", "Document non trouvé"));
        }

        Document doc = docOpt.get();

        // Vérifier que le document appartient à l'utilisateur
        if (!doc.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(403).body(Map.of("error", "Accès refusé"));
        }

        // Supprimer le fichier
        try {
            Path filePath = Paths.get(UPLOAD_DIR).resolve(doc.getFileName());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de la suppression du fichier"));
        }

        // Supprimer de la BDD
        documentRepository.delete(doc);

        return ResponseEntity.ok(Map.of("message", "Document supprimé"));
    }
}
