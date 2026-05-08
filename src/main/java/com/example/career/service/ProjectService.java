package com.example.career.service;

import com.example.career.dto.ProjectDto;
import com.example.career.mapper.ProjectMapper;
import com.example.career.model.Project;
import com.example.career.model.RequiredSkill;
import com.example.career.model.SkillCategoryRequirement;
import com.example.career.repository.ProjectRepository;
import com.example.career.repository.RequiredSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.example.career.model.Document;
import com.example.career.model.ProjectMatching;
import com.example.career.model.User;
import com.example.career.repository.DocumentRepository;
import com.example.career.repository.ProjectMatchingRepository;
import com.example.career.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private RequiredSkillRepository skillRepository;

    @Autowired
    private ProjectMatchingRepository matchingRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DocumentRepository documentRepository;

    // Jackson object mapper for reading JSON files and serializing payloads
    @Autowired
    private ObjectMapper objectMapper;

    @Value("${matching.microservice.url:}")
    private String matchingMicroserviceUrl;

    @Value("${ai.service.url:}")
    private String aiServiceUrl;

    /**
     * Get all projects
     */
    public List<ProjectDto> getAllProjects() {
        List<Project> projects = projectRepository.findAll();
        return projectMapper.toDtoList(projects);
    }

    /**
     * Get all projects for a specific manager
     */
    public List<ProjectDto> getProjectsByManager(String managerEmail) {
        List<Project> projects = projectRepository.findByManager(managerEmail);
        return projectMapper.toDtoList(projects);
    }

    /**
     * Get project by ID
     */
    public ProjectDto getProjectById(Long id) {
        Optional<Project> project = projectRepository.findById(id);
        return project.map(projectMapper::toDto).orElse(null);
    }

    /**
     * Create a new project
     */
    public ProjectDto createProject(ProjectDto projectDto) {
        validateProject(projectDto);
        
        Project project = projectMapper.toEntity(projectDto);
        
        // Handle skills association
        if (projectDto.getRequiredSkills() != null && !projectDto.getRequiredSkills().isEmpty()) {
            projectDto.getRequiredSkills().forEach(skillDto -> {
                RequiredSkill skill = new RequiredSkill(
                        skillDto.getSkillName(),
                        skillDto.getLevel(),
                        skillDto.getCount(),
                        skillDto.getCriticality(),
                        skillDto.getDomain(),
                        skillDto.getFamily(),
                        skillDto.getCategory(),
                        skillDto.getType()
                );
                project.addRequiredSkill(skill);
            });
        }
        // Handle category requirements
        if (projectDto.getCategoryRequirements() != null && !projectDto.getCategoryRequirements().isEmpty()) {
            projectDto.getCategoryRequirements().forEach(crDto -> {
                SkillCategoryRequirement req = new SkillCategoryRequirement();
                req.setFilterType(crDto.getFilterType());
                req.setFilterValue(crDto.getFilterValue());
                req.setDescription(crDto.getDescription());
                req.setMinCriticality(crDto.getMinCriticality());
                project.addCategoryRequirement(req);
            });
        }

        Project savedProject = projectRepository.save(project);
        return projectMapper.toDto(savedProject);
    }

    /**
     * Update an existing project
     */
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Optional<Project> existingProject = projectRepository.findById(id);

        if (existingProject.isEmpty()) {
            return null;
        }

        validateProject(projectDto);

        Project project = existingProject.get();
        project.setName(projectDto.getName());
        project.setManager(projectDto.getManager());
        project.setStartDate(projectDto.getStartDate());
        project.setEndDate(projectDto.getEndDate());
        project.setStatus(projectDto.getStatus());
        project.setDescription(projectDto.getDescription());

        // Handle skills update
        if (projectDto.getRequiredSkills() != null) {
            project.getRequiredSkills().clear();
            projectDto.getRequiredSkills().forEach(skillDto -> {
                RequiredSkill skill = new RequiredSkill(
                        skillDto.getSkillName(),
                        skillDto.getLevel(),
                        skillDto.getCount(),
                        skillDto.getCriticality(),
                        skillDto.getDomain(),
                        skillDto.getFamily(),
                        skillDto.getCategory(),
                        skillDto.getType()
                );
                project.addRequiredSkill(skill);
            });
        }
        // Handle category requirements update
        if (projectDto.getCategoryRequirements() != null) {
            project.getCategoryRequirements().clear();
            projectDto.getCategoryRequirements().forEach(crDto -> {
                SkillCategoryRequirement req = new SkillCategoryRequirement();
                req.setFilterType(crDto.getFilterType());
                req.setFilterValue(crDto.getFilterValue());
                req.setDescription(crDto.getDescription());
                req.setMinCriticality(crDto.getMinCriticality());
                project.addCategoryRequirement(req);
            });
        }

        Project updatedProject = projectRepository.save(project);
        return projectMapper.toDto(updatedProject);
    }

    /**
     * Delete a project
     */
    public boolean deleteProject(Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }

    /**
     * Simple matching logic - counts matching skills between project and candidate profile
     * (stub for integration with AI microservice)
     */
    public MatchingResult getMatchingCandidates(Long projectId) {
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty()) {
            return null;
        }

        Project project = projectOpt.get();
        ProjectDto projectDto = projectMapper.toDto(project);

        // build payload for microservice
        Map<String,Object> payload = new HashMap<>();
        payload.put("project", convertProjectForMicroservice(projectDto));
        payload.put("employees", loadEmployeeData());

        try {
            RestTemplate rest = new RestTemplate();
            // log payload for debugging (especially level normalization)
            System.out.println("[ProjectService] payload to microservice: " + payload);
            String url = getMatchingMicroserviceUrl();
            System.out.println("[ProjectService] calling microservice at: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String,Object>> requestEntity = new HttpEntity<>(payload, headers);

            ResponseEntity<MatchingResult> response = rest.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    MatchingResult.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new IllegalStateException("Matching service returned non-success response: " + response.getStatusCode());
            }

            MatchingResult result = response.getBody();

            // Normalize/massage the response for our frontend needs
            normalizeMatchingResult(result);

            // Persist the matching result
            saveMatchingResult(project, result);

            return result;
        } catch (RestClientException ex) {
            System.err.println("[ProjectService] Microservice request failed: " + ex.getMessage());
            throw new IllegalStateException("Matching microservice unavailable", ex);
        } catch (Exception ex) {
            System.err.println("[ProjectService] Unexpected error while calling matching microservice: " + ex.getMessage());
            throw new IllegalStateException("Unexpected error while calling matching microservice", ex);
        }
    }

    /**
     * Normalize the matching result for frontend consumption.
     *
     * - Limit missing skills to original requirements (max 10)
     * - Add direct/related/semantic score fields for easier display
     */
    @SuppressWarnings("unchecked")
    private void normalizeMatchingResult(MatchingResult result) {
        if (result == null || result.getMatches() == null) {
            return;
        }

        for (Map<String, Object> match : result.getMatches()) {
            Map<String, Object> matchDetails = null;
            if (match.get("match_details") instanceof Map) {
                matchDetails = (Map<String, Object>) match.get("match_details");
            }

            // expose scores on the top-level match object for easier frontend display
            Object directScore = match.get("direct_score");
            Object relatedScore = match.get("related_score");
            Object semanticScore = match.get("semantic_score");

            if (matchDetails != null) {
                if (directScore == null) {
                    directScore = matchDetails.get("direct_score");
                }
                if (relatedScore == null) {
                    relatedScore = matchDetails.get("related_score");
                }
                if (semanticScore == null) {
                    if (matchDetails.get("matched_semantic_skills") instanceof List) {
                        List<?> semanticMatches = (List<?>) matchDetails.get("matched_semantic_skills");
                        int totalReqs = 0;
                        if (match.get("matched_requirements") instanceof List) {
                            totalReqs = ((List<?>) match.get("matched_requirements")).size();
                        }
                        if (totalReqs > 0) {
                            semanticScore = (double) semanticMatches.size() / totalReqs;
                        }
                    }
                }
            }

            if (directScore != null) {
                match.put("direct_skill_score", directScore);
            }
            if (relatedScore != null) {
                match.put("related_skill_score", relatedScore);
            }
            if (semanticScore != null) {
                match.put("semantic_skill_score", semanticScore);
            }

            // Compute missing skills based on original requirements (not expanded skills)
            List<String> missing = new ArrayList<>();
            if (match.get("matched_requirements") instanceof List) {
                for (Object reqObj : (List<?>) match.get("matched_requirements")) {
                    if (!(reqObj instanceof Map)) continue;
                    Map<?, ?> reqMap = (Map<?, ?>) reqObj;
                    if (!(reqMap.get("requirement") instanceof Map)) continue;

                    Map<?, ?> reqRequirement = (Map<?, ?>) reqMap.get("requirement");
                    Object skillNameObj = reqRequirement.get("skill_name");
                    if (skillNameObj == null) {
                        continue;
                    }
                    String skillName = skillNameObj.toString();

                    boolean matchedDirect = false;
                    if (reqMap.get("match_details") instanceof Map) {
                        Map<?, ?> reqMatchDetails = (Map<?, ?>) reqMap.get("match_details");
                        if (reqMatchDetails.get("direct_matches") instanceof List) {
                            for (Object dm : (List<?>) reqMatchDetails.get("direct_matches")) {
                                if (dm != null && skillName.equalsIgnoreCase(dm.toString())) {
                                    matchedDirect = true;
                                    break;
                                }
                            }
                        }
                    }
                    if (!matchedDirect) {
                        missing.add(skillName);
                    }
                }
            }

            if (missing.size() > 10) {
                missing = missing.subList(0, 10);
            }
            match.put("missing_skills", missing);
        }
    }

    /**
     * Save matching result to database
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    private void saveMatchingResult(Project project, MatchingResult result) {
        try {
            // Convert matches list to JSON string
            String matchesData = null;
            if (result.getMatches() != null && !result.getMatches().isEmpty()) {
                matchesData = objectMapper.writeValueAsString(result.getMatches());
            }

            ProjectMatching matching = new ProjectMatching(
                project,
                result.getTotalCandidates(),
                result.getSkillCount(),
                result.getMatchingScore(),
                result.getMessage(),
                matchesData
            );

            matchingRepository.save(matching);
        } catch (Exception e) {
            System.err.println("Error saving matching result: " + e.getMessage());
            // Don't throw exception to avoid breaking the matching flow
        }
    }

    /**
     * Get matching history for a project
     */
    public List<ProjectMatching> getMatchingHistory(Long projectId) {
        return matchingRepository.findByProjectIdOrderByCreatedAtDesc(projectId);
    }

    /**
     * Get latest matching result for a project
     */
    public ProjectMatching getLatestMatching(Long projectId) {
        List<ProjectMatching> matchings = matchingRepository.findTop10ByProjectIdOrderByCreatedAtDesc(projectId);
        return matchings.isEmpty() ? null : matchings.get(0);
    }

    /**
     * Build the matching microservice URL, using environment variable override if available.
     * If no override is provided, detect the running service on localhost ports 8000 or 8001.
     */
    private String getMatchingMicroserviceUrl() {
        String envUrl = System.getenv("MATCHING_MICROSERVICE_URL");
        String configuredUrl = null;

        if (matchingMicroserviceUrl != null && !matchingMicroserviceUrl.isBlank()) {
            configuredUrl = matchingMicroserviceUrl.trim();
        } else if (aiServiceUrl != null && !aiServiceUrl.isBlank()) {
            configuredUrl = aiServiceUrl.trim();
        } else if (envUrl != null && !envUrl.isBlank()) {
            configuredUrl = envUrl.trim();
        }

        if (configuredUrl != null && !configuredUrl.isBlank()) {
            configuredUrl = configuredUrl.replaceAll("/+$", "");
            if (configuredUrl.matches("(?i).*/match/inline$")) {
                return configuredUrl;
            }
            if (configuredUrl.endsWith("/api/v1")) {
                return configuredUrl + "/match/inline";
            }
            if (configuredUrl.endsWith("/api/v1/")) {
                return configuredUrl + "match/inline";
            }
            return configuredUrl + "/api/v1/match/inline";
        }

        return detectMatchingMicroserviceUrl();
    }

    private String detectMatchingMicroserviceUrl() {
        RestTemplate rest = new RestTemplate();
        String[] candidates = {"http://localhost:8000", "http://localhost:8001"};
        for (String baseUrl : candidates) {
            String probeUrl = baseUrl + "/api/v1/employees";
            try {
                ResponseEntity<String> response = rest.getForEntity(probeUrl, String.class);
                if (response.getStatusCode().is2xxSuccessful()) {
                    System.out.println("[ProjectService] detected matching microservice on: " + baseUrl);
                    return baseUrl + "/api/v1/match/inline";
                }
            } catch (Exception e) {
                System.out.println("[ProjectService] matching service probe failed for " + probeUrl + ": " + e.getMessage());
            }
        }
        System.out.println("[ProjectService] using default matching microservice URL http://localhost:8001/api/v1/match/inline");
        return "http://localhost:8001/api/v1/match/inline";
    }

    /**
     * Load employees from the database and convert to microservice format.
     * For each user with a CV, parse their aiReport to extract skills.
     */
    private List<Map<String,Object>> loadEmployeeData() {
        List<User> users = userRepository.findAll();
        List<Map<String,Object>> result = new ArrayList<>();
        for (User user : users) {
            Map<String,Object> emp = new HashMap<>();
            emp.put("id", "emp" + user.getId());
            String fullName = ((user.getPrenom() != null ? user.getPrenom() : "") + " " +
                    (user.getNom() != null ? user.getNom() : "")).trim();
            emp.put("name", fullName.isEmpty() ? user.getEmail() : fullName);

            // Parse skills from the latest CV aiReport
            List<Map<String,Object>> skills = new ArrayList<>();
            List<Document> cvDocs = documentRepository.findByUserIdAndDocumentType(
                    user.getId(), Document.DocumentType.CV);
            if (!cvDocs.isEmpty()) {
                Document latestCv = cvDocs.get(cvDocs.size() - 1);
                if (latestCv.getAiReport() != null && !latestCv.getAiReport().isBlank()) {
                    try {
                        Map<String,Object> report = objectMapper.readValue(
                                latestCv.getAiReport(), new TypeReference<Map<String,Object>>(){});
                        Object rawSkills = report.get("skills");
                        if (rawSkills instanceof List<?>) {
                            for (Object s : (List<?>) rawSkills) {
                                if (!(s instanceof Map)) continue;
                                @SuppressWarnings("unchecked")
                                Map<String,Object> sk = (Map<String,Object>) s;
                                Map<String,Object> convertedSkill = new HashMap<>();
                                convertedSkill.put("skill_name", sk.getOrDefault("name", ""));
                                String lvl = sk.getOrDefault("level", "Junior").toString();
                                lvl = normalizeSkillLevel(lvl);
                                convertedSkill.put("level", lvl);
                                Object yrs = sk.getOrDefault("years_experience", 0.0);
                                double yearsExp = (yrs instanceof Number) ? ((Number)yrs).doubleValue() : 0.0;
                                convertedSkill.put("years_experience", yearsExp);
                                skills.add(convertedSkill);
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("[ProjectService] Could not parse aiReport for user " + user.getId() + ": " + e.getMessage());
                    }
                }
            }
            emp.put("skills", skills);

            double maxExp = skills.stream()
                    .mapToDouble(s -> ((Number) s.getOrDefault("years_experience", 0.0)).doubleValue())
                    .max().orElse(0.0);
            emp.put("experience_years", maxExp);

            result.add(emp);
        }
        return result;
    }

    /**
     * Convert employee format from backend JSON to microservice format.
     * Backend JSON: id, name, department, skills[{name, level, yearsExperience}]
     * Microservice: id, name, experience_years, skills[{skill_name, level, years_experience}]
     */
    @SuppressWarnings("unchecked")
    // made public for unit testing
    public List<Map<String,Object>> convertEmployeesForMicroservice(List<Map<String,Object>> backendEmployees) {
        return backendEmployees.stream().map(emp -> {
            Map<String,Object> converted = new HashMap<>();
            
            // Convert id to string format for microservice
            Object idObj = emp.get("id");
            converted.put("id", "emp" + (idObj instanceof Number ? ((Number)idObj).intValue() : idObj));
            
            // Keep name as is
            converted.put("name", emp.get("name"));
            
            // Calculate total experience years from skills
            double totalExperience = 0.0;
            if (emp.get("skills") instanceof List) {
                List<Map<String,Object>> skills = (List<Map<String,Object>>) emp.get("skills");
                for (Map<String,Object> skill : skills) {
                    Object exp = skill.get("yearsExperience");
                    if (exp instanceof Number) {
                        totalExperience = Math.max(totalExperience, ((Number)exp).doubleValue());
                    }
                }
            }
            converted.put("experience_years", totalExperience);
            
            // Convert skills array
            List<Map<String,Object>> convertedSkills = new ArrayList<>();
            if (emp.get("skills") instanceof List) {
                List<Map<String,Object>> skills = (List<Map<String,Object>>) emp.get("skills");
                for (Map<String,Object> skill : skills) {
                    Map<String,Object> convertedSkill = new HashMap<>();
                    convertedSkill.put("skill_name", skill.get("name"));

                    // normalize level strings for microservice (enum: Junior, Intermediate, Senior, Expert)
                    Object lvl = skill.get("level");
                    String levelStr = lvl == null ? "Junior" : lvl.toString();
                    levelStr = normalizeSkillLevel(levelStr);
                    convertedSkill.put("level", levelStr);

                    Object exp = skill.get("yearsExperience");
                    double yearsExp = (exp instanceof Number) ? ((Number)exp).doubleValue() : 0.0;
                    convertedSkill.put("years_experience", yearsExp);

                    convertedSkills.add(convertedSkill);
                }
            }
            converted.put("skills", convertedSkills);
            
            return converted;
        }).toList();
    }

    /**
     * Convert the internal ProjectDto into a simple map structure compatible
     * with the Python microservice's Project model.
     */
    /**
     * Normalize a skill level string to one of the valid microservice enum values:
     * Junior, Intermediate, Senior, Expert.
     */
    private String normalizeSkillLevel(String level) {
        if (level == null || level.isBlank()) return "Junior";
        switch (level.trim().toLowerCase()) {
            case "junior": case "beginner": case "basic": case "entry": case "novice": case "débutant":
                return "Junior";
            case "intermediate": case "mid": case "middle": case "medium": case "moyen": case "intermédiaire":
                return "Intermediate";
            case "senior": case "advanced": case "avancé": case "confirmé":
                return "Senior";
            case "expert": case "master": case "lead": case "principal":
                return "Expert";
            default:
                return "Junior";
        }
    }

    /* package-private for unit testing */
    Map<String,Object> convertProjectForMicroservice(ProjectDto dto) {
        Map<String,Object> map = new HashMap<>();
        map.put("id", dto.getId().toString());
        map.put("name", dto.getName());
        List<Map<String,Object>> reqs = new ArrayList<>();
        if (dto.getRequiredSkills() != null) {
            dto.getRequiredSkills().forEach(s -> {
                Map<String,Object> r = new HashMap<>();
                r.put("skill_name", s.getSkillName());
                if (s.getCriticality() != null) r.put("criticality", s.getCriticality());
                if (s.getDomain() != null) r.put("domain", s.getDomain());
                if (s.getFamily() != null) r.put("family", s.getFamily());
                if (s.getCategory() != null) r.put("category", s.getCategory());
                if (s.getType() != null) r.put("type", s.getType());
                reqs.add(r);
            });
        }
        if (dto.getCategoryRequirements() != null) {
            dto.getCategoryRequirements().forEach(cr -> {
                Map<String,Object> r = new HashMap<>();
                switch (cr.getFilterType()) {
                    case "domain": r.put("domain", cr.getFilterValue()); break;
                    case "family": r.put("family", cr.getFilterValue()); break;
                    case "category": r.put("category", cr.getFilterValue()); break;
                    case "type": r.put("type", cr.getFilterValue()); break;
                }
                if (cr.getMinCriticality() != null) r.put("min_criticality", cr.getMinCriticality());
                reqs.add(r);
            });
        }
        map.put("requirements", reqs);
        return map;
    }

    /**
     * Validate project data
     */
    private void validateProject(ProjectDto projectDto) {
        if (projectDto.getName() == null || projectDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Project name is required");
        }
        if (projectDto.getManager() == null || projectDto.getManager().trim().isEmpty()) {
            throw new IllegalArgumentException("Project manager is required");
        }
        if (projectDto.getStartDate() == null) {
            throw new IllegalArgumentException("Start date is required");
        }
    }

    /**
     * Inner class for matching results
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatchingResult {
        @JsonAlias("project_id")
        private String projectId;
        @JsonAlias("project_name")
        private String projectName;
        private int skillCount;
        private double matchingScore;
        private String message;

        // new fields coming from microservice response
        @JsonAlias("total_candidates")
        private Integer totalCandidates;
        private List<Map<String,Object>> matches;

        public String getProjectId() {
            return projectId;
        }
        
        public void setProjectId(String projectId) {
            this.projectId = projectId;
        }
        
        public String getProjectName() {
            return projectName;
        }
        
        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }
        
        public int getSkillCount() {
            return skillCount;
        }
        
        public void setSkillCount(int skillCount) {
            this.skillCount = skillCount;
        }
        
        public double getMatchingScore() {
            return matchingScore;
        }
        
        public void setMatchingScore(double matchingScore) {
            this.matchingScore = matchingScore;
        }
        
        public Integer getTotalCandidates() {
            return totalCandidates;
        }
        
        public void setTotalCandidates(Integer totalCandidates) {
            this.totalCandidates = totalCandidates;
        }
        
        public List<Map<String,Object>> getMatches() {
            return matches;
        }
        
        public void setMatches(List<Map<String,Object>> matches) {
            this.matches = matches;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
    }
}
