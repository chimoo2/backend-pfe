package com.example.career.config;

import com.example.career.model.Project;
import com.example.career.model.RequiredSkill;
import com.example.career.model.Role;
import com.example.career.model.SkillTaxonomy;
import com.example.career.model.User;
import com.example.career.repository.ProjectRepository;
import com.example.career.repository.SkillTaxonomyRepository;
import com.example.career.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * DataInitializer - Load demo dataset on application startup
 * This initializer loads employees, projects and skill taxonomy from JSON files
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final ProjectRepository projectRepository;
    private final SkillTaxonomyRepository skillTaxonomyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    // We can't rely on @PostConstruct for transactional behavior because
    // Spring proxies aren't applied yet. Instead we listen for the
    // ContextRefreshedEvent which is fired after the context is ready.
    // The listener method is annotated @Transactional so all repository calls
    // happen in a valid session.
    @EventListener(ContextRefreshedEvent.class)
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            log.info("Loading demo dataset and skill taxonomy from resources/data/");

            // Load skill taxonomy first
            loadSkillTaxonomy();

            // Load projects within the same transaction so that any lazy
            // collections can be fetched without errors.
            loadProjects();

            // Ensure an admin account exists for management and onboarding
            createDefaultAdminIfNeeded();

            // Create test manager accounts for demo purposes
            createTestManagersIfNeeded();

            log.info("Dataset initialization completed successfully");

        } catch (Exception e) {
            log.error("Failed to load demo dataset", e);
        }
    }

    @Value("${admin.default.email:admin@company.com}")
    private String defaultAdminEmail;

    @Value("${admin.default.password:ChangeMe123!}")
    private String defaultAdminPassword;

    private void createDefaultAdminIfNeeded() {
        if (userRepository.findByEmail(defaultAdminEmail).isPresent()) {
            return;
        }

        User admin = new User();
        admin.setPrenom("Admin");
        admin.setNom("System");
        admin.setEmail(defaultAdminEmail);
        admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
        admin.setRole(Role.ROLE_ADMIN);

        userRepository.save(admin);
        log.info("Created default admin user [{}] with default password (change immediately)", defaultAdminEmail);
    }

    private void createTestManagersIfNeeded() {
        // Create test manager accounts that correspond to the project managers
        Map<String, String[]> testManagers = Map.of(
            "john.smith@company.com", new String[]{"John", "Smith"},
            "jane.doe@company.com", new String[]{"Jane", "Doe"},
            "robert.johnson@company.com", new String[]{"Robert", "Johnson"},
            "lisa.wang@company.com", new String[]{"Lisa", "Wang"},
            "mike.brown@company.com", new String[]{"Mike", "Brown"},
            "sarah.davis@company.com", new String[]{"Sarah", "Davis"}
        );

        for (Map.Entry<String, String[]> entry : testManagers.entrySet()) {
            String email = entry.getKey();
            String[] names = entry.getValue();

            if (userRepository.findByEmail(email).isEmpty()) {
                User manager = new User();
                manager.setPrenom(names[0]);
                manager.setNom(names[1]);
                manager.setEmail(email);
                manager.setPassword(passwordEncoder.encode("Manager123!"));
                manager.setRole(Role.ROLE_MANAGER);

                userRepository.save(manager);
                log.info("Created test manager user [{}]", email);
            }
        }
    }

    private void loadSkillTaxonomy() {
        try {
            long existingCount = skillTaxonomyRepository.count();
            if (existingCount > 0) {
                log.info("Skill taxonomy already loaded. Skipping. ({} skills found)", existingCount);
                return;
            }

            ClassPathResource resource = new ClassPathResource("data/skills_taxonomy.json");
            InputStream inputStream = resource.getInputStream();
            Map<String, Map<String, Object>> taxonomyData = objectMapper.readValue(
                    inputStream,
                    new TypeReference<Map<String, Map<String, Object>>>() {}
            );

            taxonomyData.forEach((skillName, skillData) -> {
                SkillTaxonomy skill = SkillTaxonomy.builder()
                        .skillName(skillName)
                        .domain((String) skillData.get("domain"))
                        .family((String) skillData.get("family"))
                        .category((String) skillData.get("category"))
                        .type((String) skillData.get("type"))
                        .criticality(((Number) skillData.get("criticality")).intValue())
                        .build();

                skillTaxonomyRepository.save(skill);
            });

            log.info("Loaded {} skills into taxonomy", taxonomyData.size());

        } catch (Exception e) {
            log.error("Error loading skill taxonomy from JSON", e);
        }
    }

  @Transactional
private void loadProjects() {
    try {

        long existingCount = projectRepository.count();

        if (existingCount > 0) {
            log.info("Projects already exist in database. Skipping sample loading.");
            return;
        }

        ClassPathResource resource = new ClassPathResource("data/projects.json");
        InputStream inputStream = resource.getInputStream();

        List<Project> projects = objectMapper.readValue(
                inputStream,
                new TypeReference<List<Project>>() {}
        );

        for (Project project : projects) {

            if (project.getRequiredSkills() != null) {
                for (RequiredSkill skill : project.getRequiredSkills()) {
                    skill.setProject(project);
                }
            }

            if (project.getCategoryRequirements() != null) {
                project.getCategoryRequirements().forEach(req -> req.setProject(project));
            }
        }

        projectRepository.saveAll(projects);

        log.info("Loaded {} projects into database", projects.size());

    } catch (Exception e) {
        log.error("Error loading projects", e);
    }
}
}
