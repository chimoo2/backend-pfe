package com.example.career.controller;

import com.example.career.model.SkillTaxonomy;
import com.example.career.repository.SkillTaxonomyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SkillController - Provides access to the skill taxonomy
 * Allows frontend to fetch skills by family/domain without manual entry
 */
@Slf4j
@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class SkillController {

    private final SkillTaxonomyRepository skillTaxonomyRepo;

    /**
     * Get all available skill families
     * Example: GET /api/skills/families
     * Response: ["Backend", "Frontend", "DevOps", "Data Science", "QA"]
     */
    @GetMapping("/families")
    public ResponseEntity<List<String>> getAllFamilies() {
        log.debug("Fetching all skill families");
        List<String> families = skillTaxonomyRepo.findDistinctFamilies();
        return ResponseEntity.ok(families);
    }

    /**
     * Get all available skill domains
     * Example: GET /api/skills/domains
     * Response: ["Software Engineering", "Data & AI", "Infrastructure"]
     */
    @GetMapping("/domains")
    public ResponseEntity<List<String>> getAllDomains() {
        log.debug("Fetching all skill domains");
        List<String> domains = skillTaxonomyRepo.findDistinctDomains();
        return ResponseEntity.ok(domains);
    }

    /**
     * Get skills by family
     * Example: GET /api/skills?family=Backend
     * Returns all skills in the Backend family with their metadata
     */
    @GetMapping
    public ResponseEntity<List<SkillTaxonomy>> getSkillsByFamily(
            @RequestParam(required = false) String family,
            @RequestParam(required = false) String domain) {

        List<SkillTaxonomy> skills;

        if (family != null && !family.isEmpty()) {
            log.debug("Fetching skills for family: {}", family);
            skills = skillTaxonomyRepo.findByFamily(family);
        } else if (domain != null && !domain.isEmpty()) {
            log.debug("Fetching skills for domain: {}", domain);
            skills = skillTaxonomyRepo.findByDomain(domain);
        } else {
            // Return top technical skills if no filter
            log.debug("Fetching high-criticality skills");
            skills = skillTaxonomyRepo.findByCriticalityGreaterOrEqual(4);
        }

        return ResponseEntity.ok(skills);
    }

    /**
     * Get skills by multiple families
     * Example: GET /api/skills/batch?families=Backend,Frontend
     * Returns all skills from both families
     */
    @GetMapping("/batch")
    public ResponseEntity<List<SkillTaxonomy>> getSkillsByFamilies(
            @RequestParam(required = false) List<String> families) {

        if (families == null || families.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        log.debug("Fetching skills for families: {}", families);
        List<SkillTaxonomy> skills = skillTaxonomyRepo.findByFamilies(families);
        return ResponseEntity.ok(skills);
    }

    /**
     * Get a specific skill by name
     * Example: GET /api/skills/Java
     */
    @GetMapping("/{skillName}")
    public ResponseEntity<SkillTaxonomy> getSkillByName(@PathVariable String skillName) {
        log.debug("Fetching skill: {}", skillName);
        return skillTaxonomyRepo.findById(skillName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Search skills by type
     * Example: GET /api/skills/search?type=Technical
     */
    @GetMapping("/search")
    public ResponseEntity<List<SkillTaxonomy>> searchByType(
            @RequestParam String type) {
        log.debug("Searching skills by type: {}", type);
        List<SkillTaxonomy> skills = skillTaxonomyRepo.findByType(type);
        return ResponseEntity.ok(skills);
    }
}
