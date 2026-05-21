package com.example.career.controller;

import com.example.career.model.Document;
import com.example.career.model.User;
import com.example.career.repository.UserRepository;
import com.example.career.service.CvService;
import com.example.career.dto.UserDto;
import com.example.career.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/profile")
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
    public ResponseEntity<UserDto> getProfile(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return ResponseEntity.ok(UserMapper.toDto(user));
    }
    // Permet à chaque utilisateur de modifier son propre profil
    @PutMapping
    public ResponseEntity<?> updateOwnProfile(@RequestBody com.example.career.dto.UpdateUserRequest request, org.springframework.security.core.Authentication authentication) {
        System.out.println("[DEBUG] Reçu updateOwnProfile: " + request);
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElse(null);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }
            user.setPrenom(request.getPrenom());
            user.setNom(request.getNom());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setCompany(request.getCompany());
            user.setCurrentRole(request.getCurrentRole());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(request.getPassword());
            }
            userRepository.save(user);
            return ResponseEntity.ok(UserMapper.toDto(user));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
