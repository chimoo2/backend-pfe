package com.example.career.service;

import com.example.career.model.UserSkill;
import com.example.career.model.Document;
import com.example.career.service.AiExtractionService;
import com.example.career.repository.UserSkillRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
import java.util.Optional;

@Service
public class CvService {

    @Value("${cv.upload.dir:uploads/cvs/}")
    private String uploadDir;
   
    
    private final DocumentRepository documentRepository;
    private final AiExtractionService aiExtractionService;
    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final ObjectMapper objectMapper;

    public CvService(
            DocumentRepository documentRepository,
            AiExtractionService aiExtractionService,
            UserRepository userRepository,
            UserSkillRepository userSkillRepository,
            ObjectMapper objectMapper
    ) {
        this.documentRepository = documentRepository;
        this.aiExtractionService = aiExtractionService;
        this.userRepository = userRepository;
        this.userSkillRepository = userSkillRepository;
        this.objectMapper = objectMapper;
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
          

        // 1️⃣ Sauvegarder en base
        Document savedDoc = documentRepository.save(doc);

// 2️⃣ Convertir le chemin en fichier physique
        File physicalFile = filePath.toFile();

// 3️⃣ Appeler le microservice IA
        String aiResponse = aiExtractionService.extractSkills(physicalFile);

// 4️⃣ Sauvegarder le JSON retourné
        savedDoc.setAiReport(aiResponse);
        documentRepository.save(savedDoc);

// 4bis️⃣ Persister automatiquement les skills extraites dans user_skill
        persistExtractedSkillsToUserSkill(user, aiResponse);

// 5️⃣ Retourner le document final
        return savedDoc;
    }

    private void persistExtractedSkillsToUserSkill(User user, String aiResponse) {
        if (user == null || aiResponse == null || aiResponse.isBlank()) {
            return;
        }

        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            JsonNode skillsNode = root.path("skills");
            if (!skillsNode.isArray()) {
                return;
            }

            for (JsonNode skillNode : skillsNode) {
                String skillName = text(skillNode, "skill_name");
                if (skillName.isBlank()) {
                    skillName = text(skillNode, "name");
                }
                if (skillName.isBlank()) {
                    continue;
                }

                Optional<UserSkill> existing = userSkillRepository.findByUserAndSkillName(user, skillName);
                UserSkill userSkill = existing.orElseGet(UserSkill::new);

                userSkill.setUser(user);
                userSkill.setSkillName(skillName);
                userSkill.setCategory(text(skillNode, "category"));
                userSkill.setFamily(text(skillNode, "family"));
                userSkill.setType(text(skillNode, "type"));
                userSkill.setLevel(text(skillNode, "level"));
                userSkill.setDomain(text(skillNode, "domain"));

                int experience = intValue(skillNode, "experience");
                if (experience == 0) {
                    experience = intValue(skillNode, "years_experience");
                }
                userSkill.setExperience(experience);

                userSkillRepository.save(userSkill);
            }
        } catch (Exception e) {
            System.err.println("[CvService] Unable to persist extracted skills to user_skill: " + e.getMessage());
        }
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? "" : value.asText("").trim();
    }

    private int intValue(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return 0;
        }
        if (value.isNumber()) {
            return value.asInt();
        }
        try {
            return Integer.parseInt(value.asText("0").trim());
        } catch (NumberFormatException ex) {
            return 0;
        }
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
