package com.example.career.controller;

import com.example.career.model.User;
import com.example.career.model.UserSkill;
import com.example.career.repository.UserRepository;
import com.example.career.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-skills")
@RequiredArgsConstructor
public class UserSkillController {
    private final UserSkillRepository userSkillRepository;
    private final UserRepository userRepository;

    // Récupérer toutes les skills d'un utilisateur connecté
    @GetMapping
    public ResponseEntity<List<UserSkill>> getUserSkills(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(userSkillRepository.findByUser(user));
    }

    // Ajouter une skill à l'utilisateur connecté
    @PostMapping
    public ResponseEntity<UserSkill> addUserSkill(@RequestBody UserSkill skill, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        String skillName = skill.getSkillName();
        if (skillName == null || skillName.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        UserSkill existing = userSkillRepository.findByUserAndSkillName(user, skillName).orElse(null);
        UserSkill toSave = existing != null ? existing : new UserSkill();

        toSave.setUser(user);
        toSave.setSkillName(skillName);
        toSave.setCategory(skill.getCategory());
        toSave.setFamily(skill.getFamily());
        toSave.setType(skill.getType());
        toSave.setLevel(skill.getLevel());
        toSave.setDomain(skill.getDomain());
        toSave.setExperience(skill.getExperience());

        UserSkill saved = userSkillRepository.save(toSave);
        return ResponseEntity.ok(saved);
    }
    @PutMapping("/{skillName}")
public ResponseEntity<UserSkill> updateUserSkill(
        @PathVariable String skillName,
        @RequestBody UserSkill updatedSkill,
        Authentication authentication
) {
    String email = authentication.getName();
    User user = userRepository.findByEmail(email).orElse(null);
    if (user == null) return ResponseEntity.notFound().build();

    UserSkill existingSkill = userSkillRepository
            .findByUserAndSkillName(user, skillName)
            .orElse(null);

    if (existingSkill == null) return ResponseEntity.notFound().build();

    existingSkill.setSkillName(updatedSkill.getSkillName());
    existingSkill.setCategory(updatedSkill.getCategory());
    existingSkill.setFamily(updatedSkill.getFamily());
    existingSkill.setType(updatedSkill.getType());
    existingSkill.setLevel(updatedSkill.getLevel());
    existingSkill.setDomain(updatedSkill.getDomain());
    existingSkill.setExperience(updatedSkill.getExperience());

    UserSkill saved = userSkillRepository.save(existingSkill);
    return ResponseEntity.ok(saved);
}

    // Supprimer une skill de l'utilisateur connecté
    @DeleteMapping("/{skillName}")
    public ResponseEntity<?> deleteUserSkill(@PathVariable String skillName, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        userSkillRepository.deleteByUserAndSkillName(user, skillName);
        return ResponseEntity.ok().build();
    }
}
