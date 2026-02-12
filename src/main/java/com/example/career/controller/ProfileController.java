package com.example.career.controller;

import com.example.career.model.Document;
import com.example.career.model.User;
import com.example.career.repository.UserRepository;
import com.example.career.service.CvService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    private final CvService cvService;
    private final UserRepository userRepository;

    public ProfileController(CvService cvService, UserRepository userRepository) {
        this.cvService = cvService;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload-cv/{userId}")
    public ResponseEntity<?> uploadCv(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {

        try {
            // Sauvegarder le CV (retourne Document avec métadonnées)
            Document doc = cvService.saveCv(file, userId);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "CV uploadé avec succès");
            response.put("documentId", doc.getId());
            response.put("cvPath", doc.getFilePath());
            response.put("fileName", doc.getOriginalFileName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", e.getMessage())
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("error", "Erreur lors de l'upload: " + e.getMessage())
            );
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(
                userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"))
        );
    }
}
