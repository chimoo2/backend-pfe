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
import com.example.career.model.Role;
import com.example.career.model.User;
import com.example.career.model.EmployeeProjectScore;
import com.example.career.model.TeamMember;
import com.example.career.model.UserSkill;
import com.example.career.repository.DocumentRepository;
import com.example.career.repository.ProjectMatchingRepository;
import com.example.career.repository.EmployeeProjectScoreRepository;
import com.example.career.repository.TeamMemberRepository;
import com.example.career.repository.UserSkillRepository;
import com.example.career.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    private EmployeeProjectScoreRepository employeeProjectScoreRepository;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

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
     * Get all users with skills for assignment modal.
     * Skills are loaded from user_skill table.
     */
    public List<Map<String, Object>> getAllEmployees() {
        List<User> users = userRepository.findAll();
        List<Map<String, Object>> employees = new ArrayList<>();

        for (User user : users) {
            if (user.getRole() != Role.ROLE_USER) {
                continue;
            }

            Map<String, Object> employee = new HashMap<>();
            employee.put("id", user.getId());
            employee.put("prenom", user.getPrenom());
            employee.put("nom", user.getNom());
            employee.put("email", user.getEmail());
            employee.put("phone", user.getPhone());
            employee.put("company", user.getCompany());
            employee.put("position", user.getPosition());
            employee.put("currentRole", user.getCurrentRole());
            employee.put("role", user.getRole() != null ? user.getRole().name() : null);

            List<Map<String, Object>> assignedProjects = new ArrayList<>();
            if (user.getEmail() != null && !user.getEmail().isBlank()) {
                List<TeamMember> assignments = teamMemberRepository.findByEmailIgnoreCaseWithProject(user.getEmail());
                Map<Long, Map<String, Object>> uniqueProjects = new HashMap<>();
                for (TeamMember assignment : assignments) {
                    if (assignment.getProject() == null || assignment.getProject().getId() == null) {
                        continue;
                    }
                    Project assignedProject = assignment.getProject();
                    Map<String, Object> p = new HashMap<>();
                    p.put("projectId", assignedProject.getId());
                    p.put("projectName", assignedProject.getName());
                    p.put("status", assignedProject.getStatus());
                    p.put("manager", assignedProject.getManager());
                    uniqueProjects.put(assignedProject.getId(), p);
                }
                assignedProjects = new ArrayList<>(uniqueProjects.values());
            }
            employee.put("assignedProjects", assignedProjects);
            employee.put("assignedProjectsCount", assignedProjects.size());

            List<UserSkill> userSkills = userSkillRepository.findByUser(user);
            List<Map<String, Object>> skills = userSkills.stream().map(skill -> {
                Map<String, Object> s = new HashMap<>();
                s.put("name", skill.getSkillName());
                s.put("level", skill.getLevel());
                s.put("years_experience", skill.getExperience() != null ? skill.getExperience() : 0);
                s.put("domain", skill.getDomain());
                s.put("family", skill.getFamily());
                s.put("category", skill.getCategory());
                s.put("type", skill.getType());
                return s;
            }).toList();

            employee.put("skills", skills);
            employees.add(employee);
        }

        return employees;
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

        // projectMapper.toEntity already maps requiredSkills and categoryRequirements
        // (Project.setRequiredSkills / setCategoryRequirements set the back-reference too)
        Project project = projectMapper.toEntity(projectDto);

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
        project.setDuration(projectDto.getDuration());
        project.setCount(projectDto.getCount());

        // Handle skills update
        if (projectDto.getRequiredSkills() != null) {
            project.getRequiredSkills().clear();
            projectDto.getRequiredSkills().forEach(skillDto -> {
                RequiredSkill skill = new RequiredSkill();
                skill.setSkillName(skillDto.getSkillName());
                skill.setCriticality(skillDto.getCriticality());
                skill.setDomain(skillDto.getDomain());
                skill.setFamily(skillDto.getFamily());
                skill.setCategory(skillDto.getCategory());
                skill.setType(skillDto.getType());
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
     * Save matching result to database, including individual employee scores
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

            // Save individual employee scores
            saveIndividualEmployeeScores(project, result);
        } catch (Exception e) {
            System.err.println("Error saving matching result: " + e.getMessage());
            // Don't throw exception to avoid breaking the matching flow
        }
    }

    /**
     * Save individual employee scores for a project
     */
    @SuppressWarnings("unchecked")
    private void saveIndividualEmployeeScores(Project project, MatchingResult result) {
        if (result == null || result.getMatches() == null || result.getMatches().isEmpty()) {
            return;
        }

        for (Map<String, Object> match : result.getMatches()) {
            try {
                String employeeId = String.valueOf(
                    match.getOrDefault("employee_id",
                        match.getOrDefault("id", match.getOrDefault("employeeId", ""))));
                String employeeName = String.valueOf(
                    match.getOrDefault("employee_name",
                        match.getOrDefault("name", match.getOrDefault("employeeName", "Unknown"))));
                
                // Extract overall score - try multiple possible field names
                Double overallScore = extractScore(match.get("overall_score"));
                if (overallScore == null) {
                    overallScore = extractScore(match.get("score"));
                }
                if (overallScore == null) {
                    overallScore = extractScore(match.get("matching_score"));
                }
                if (overallScore == null) {
                    overallScore = 0.0;
                }

                // Extract individual skill scores
                Double directScore = extractScore(match.get("direct_skill_score"));
                Double relatedScore = extractScore(match.get("related_skill_score"));
                Double semanticScore = extractScore(match.get("semantic_skill_score"));

                // Extract matched and missing skills
                String matchedSkillsJson = "";
                String missingSkillsJson = "";
                
                if (match.get("matched_requirements") instanceof List) {
                    List<?> matched = (List<?>) match.get("matched_requirements");
                    matchedSkillsJson = objectMapper.writeValueAsString(matched);
                }

                if (match.get("missing_skills") instanceof List) {
                    List<?> missing = (List<?>) match.get("missing_skills");
                    missingSkillsJson = objectMapper.writeValueAsString(missing);
                }

                // Create employee project score entity
                EmployeeProjectScore score = new EmployeeProjectScore(
                    project,
                    employeeId,
                    employeeName,
                    overallScore
                );
                
                score.setDirectSkillScore(directScore);
                score.setRelatedSkillScore(relatedScore);
                score.setSemanticSkillScore(semanticScore);
                score.setMatchedSkills(matchedSkillsJson.length() > 3000 ? matchedSkillsJson.substring(0, 3000) : matchedSkillsJson);
                score.setMissingSkills(missingSkillsJson.length() > 3000 ? missingSkillsJson.substring(0, 3000) : missingSkillsJson);
                
                // Store the full match details
                String fullDetails = objectMapper.writeValueAsString(match);
                score.setFullMatchDetails(fullDetails);

                employeeProjectScoreRepository.save(score);
                System.out.println("[ProjectService] Saved score for employee " + employeeName + ": " + overallScore);

            } catch (Exception e) {
                System.err.println("Error saving individual employee score: " + e.getMessage());
                // Continue to next employee even if one fails
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> extractRecommendedCourses(EmployeeProjectScore scoreRow) {
        List<String> courses = new ArrayList<>();
        if (scoreRow == null || scoreRow.getFullMatchDetails() == null || scoreRow.getFullMatchDetails().isBlank()) {
            return courses;
        }

        try {
            Map<String, Object> details = objectMapper.readValue(
                    scoreRow.getFullMatchDetails(), new TypeReference<Map<String, Object>>(){});

            Object raw = details.get("recommended_training");
            if (raw instanceof List<?>) {
                for (Object item : (List<?>) raw) {
                    if (item != null) {
                        String value = item.toString().trim();
                        if (!value.isBlank()) {
                            courses.add(value);
                        }
                    }
                }
            } else if (raw instanceof String) {
                String training = ((String) raw).trim();
                if (!training.isBlank()) {
                    for (String item : training.split(";\\s*")) {
                        String value = item == null ? "" : item.trim();
                        if (!value.isBlank()) {
                            courses.add(value);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
            // Keep notifications resilient even when fullMatchDetails is malformed JSON
        }

        return courses;
    }

    private List<String> extractMissingSkills(EmployeeProjectScore scoreRow) {
        List<String> result = new ArrayList<>();
        if (scoreRow == null || scoreRow.getMissingSkills() == null || scoreRow.getMissingSkills().isBlank()) {
            return result;
        }
        try {
            String json = scoreRow.getMissingSkills().trim();
            if (json.startsWith("[")) {
                List<Object> list = objectMapper.readValue(json, new TypeReference<List<Object>>(){});
                for (Object item : list) {
                    String value = skillToString(item);
                    if (!value.isBlank()) result.add(value);
                }
            } else {
                // Fallback: comma-separated
                for (String item : json.split(",")) {
                    String value = item == null ? "" : item.trim();
                    if (!value.isBlank()) result.add(value);
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private String skillToString(Object item) {
        if (item == null) return "";
        if (item instanceof String) return ((String) item).trim();
        if (item instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) item;
            for (String key : new String[]{"skill", "skill_name", "name", "skillName"}) {
                Object v = map.get(key);
                if (v != null && !v.toString().trim().isBlank()) return v.toString().trim();
            }
            return "";
        }
        return item.toString().trim();
    }

    private static class MatchingFallbackData {
        private final List<String> recommendedCourses;
        private final Double score;
        private final List<String> missingSkills;

        private MatchingFallbackData(List<String> recommendedCourses, Double score, List<String> missingSkills) {
            this.recommendedCourses = recommendedCourses;
            this.score = score;
            this.missingSkills = missingSkills;
        }
    }

    @SuppressWarnings("unchecked")
    private MatchingFallbackData extractFallbackDataFromLatestProjectMatching(Project project, User user) {
        List<String> courses = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        Double score = null;
        if (project == null || project.getId() == null || user == null || user.getId() == null) {
            return new MatchingFallbackData(courses, score, missingSkills);
        }

        String targetEmpId = "emp" + user.getId();
        String targetNumericId = String.valueOf(user.getId());

        try {
            List<ProjectMatching> matchings = matchingRepository.findTop10ByProjectIdOrderByCreatedAtDesc(project.getId());
            for (ProjectMatching matching : matchings) {
                if (matching.getMatchesData() == null || matching.getMatchesData().isBlank()) {
                    continue;
                }

                List<Map<String, Object>> matches = objectMapper.readValue(
                        matching.getMatchesData(), new TypeReference<List<Map<String, Object>>>(){});

                for (Map<String, Object> match : matches) {
                    String candidateId = String.valueOf(
                            match.getOrDefault("employee_id",
                                    match.getOrDefault("id", match.getOrDefault("employeeId", ""))));

                    if (!targetEmpId.equalsIgnoreCase(candidateId) && !targetNumericId.equals(candidateId)) {
                        continue;
                    }

                    Object rawTraining = match.get("recommended_training");
                    if (rawTraining instanceof List<?>) {
                        for (Object item : (List<?>) rawTraining) {
                            if (item != null && !item.toString().trim().isBlank()) {
                                courses.add(item.toString().trim());
                            }
                        }
                    } else if (rawTraining instanceof String) {
                        String text = ((String) rawTraining).trim();
                        if (!text.isBlank()) {
                            for (String item : text.split(";\\s*")) {
                                if (item != null && !item.trim().isBlank()) {
                                    courses.add(item.trim());
                                }
                            }
                        }
                    }

                    score = extractScore(match.get("overall_score"));
                    if (score == null) {
                        score = extractScore(match.get("score"));
                    }
                    if (score == null) {
                        score = extractScore(match.get("matching_score"));
                    }

                    Object rawMissing = match.get("missing_skills");
                    if (rawMissing instanceof List<?>) {
                        for (Object item : (List<?>) rawMissing) {
                            String value = skillToString(item);
                            if (!value.isBlank()) missingSkills.add(value);
                        }
                    } else if (rawMissing instanceof String) {
                        String text = ((String) rawMissing).trim();
                        if (!text.isBlank()) {
                            for (String item : text.split(",")) {
                                if (item != null && !item.trim().isBlank()) {
                                    missingSkills.add(item.trim());
                                }
                            }
                        }
                    }

                    if (!courses.isEmpty() || score != null || !missingSkills.isEmpty()) {
                        return new MatchingFallbackData(courses, score, missingSkills);
                    }
                }
            }
        } catch (Exception ignored) {
            // Best-effort fallback: ignore malformed history rows
        }

        return new MatchingFallbackData(courses, score, missingSkills);
    }

    /**
     * Helper method to extract score from various possible values
     */
    private Double extractScore(Object scoreObj) {
        if (scoreObj == null) {
            return null;
        }
        if (scoreObj instanceof Number) {
            return ((Number) scoreObj).doubleValue();
        }
        if (scoreObj instanceof String) {
            try {
                return Double.parseDouble((String) scoreObj);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
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
                                double monthsExp = (yrs instanceof Number) ? ((Number)yrs).doubleValue() : 0.0;
                                convertedSkill.put("months_experience", monthsExp);
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
                    .mapToDouble(s -> ((Number) s.getOrDefault("months_experience", 0.0)).doubleValue())
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
                r.put("skill_name", s.getSkillName() != null ? s.getSkillName() : "");
                r.put("domain", s.getDomain() != null ? s.getDomain() : "");
                r.put("family", s.getFamily() != null ? s.getFamily() : "");
                r.put("category", s.getCategory() != null ? s.getCategory() : "");
                r.put("type", s.getType() != null ? s.getType() : "");
                r.put("min_criticality", s.getCriticality() != null ? s.getCriticality() : 1);
                reqs.add(r);
            });
        }
        if (dto.getCategoryRequirements() != null) {
            dto.getCategoryRequirements().forEach(cr -> {
                Map<String,Object> r = new HashMap<>();
                r.put("skill_name", "");
                r.put("domain", "");
                r.put("family", "");
                r.put("category", "");
                r.put("type", "");
                
                if (cr.getFilterType() != null) {
                    switch (cr.getFilterType()) {
                        case "domain": r.put("domain", cr.getFilterValue() != null ? cr.getFilterValue() : ""); break;
                        case "family": r.put("family", cr.getFilterValue() != null ? cr.getFilterValue() : ""); break;
                        case "category": r.put("category", cr.getFilterValue() != null ? cr.getFilterValue() : ""); break;
                        case "type": r.put("type", cr.getFilterValue() != null ? cr.getFilterValue() : ""); break;
                    }
                }
                r.put("min_criticality", cr.getMinCriticality() != null ? cr.getMinCriticality() : 1);
                reqs.add(r);
            });
        }
        map.put("requirements", reqs);
        return map;
    }

    /**
     * Assign an employee (by email or id string) to a project.
     * The employeeId is stored in the project's teamMembers list if supported,
     * otherwise we simply confirm the project exists.
     */
    public ProjectDto assignEmployeeToProject(Long projectId, String employeeId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));

        // Resolve user from supported identifiers:
        // - numeric id: "21"
        // - matching id: "emp21"
        // - email: "user@example.com"
        User user = null;
        String normalizedEmployeeId = employeeId == null ? "" : employeeId.trim();
        if (!normalizedEmployeeId.isBlank()) {
            String candidateId = normalizedEmployeeId;
            if (candidateId.toLowerCase().startsWith("emp") && candidateId.length() > 3) {
                candidateId = candidateId.substring(3);
            }

            try {
                Long uid = Long.parseLong(candidateId);
                user = userRepository.findById(uid).orElse(null);
            } catch (NumberFormatException e) {
                user = userRepository.findByEmail(normalizedEmployeeId).orElse(null);
            }
        }

        if (user == null) {
            throw new IllegalArgumentException("Employee not found: " + employeeId);
        }

        String userEmail = user.getEmail() == null ? "" : user.getEmail().trim();
        boolean alreadyAssigned = project.getTeamMembers() != null && project.getTeamMembers().stream()
                .anyMatch(tm -> tm.getEmail() != null && tm.getEmail().equalsIgnoreCase(userEmail));

        if (!alreadyAssigned) {
            TeamMember teamMember = new TeamMember();
            teamMember.setFirstName(user.getPrenom());
            teamMember.setLastName(user.getNom());
            teamMember.setEmail(user.getEmail());
            teamMember.setRole(user.getCurrentRole() != null && !user.getCurrentRole().isBlank()
                    ? user.getCurrentRole()
                    : user.getPosition());
            project.addTeamMember(teamMember);
        }

        Project savedProject = projectRepository.save(project);
        return projectMapper.toDto(savedProject);
    }

    /**
     * Get all projects assigned to a specific user (by userId).
     * Returns all projects where the user is a team member or matching candidate.
     */
    public List<ProjectDto> getAssignedProjectsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        List<TeamMember> assignments = teamMemberRepository.findByEmailIgnoreCaseWithProject(user.getEmail());
        Set<Project> assignedProjects = new LinkedHashSet<>();
        for (TeamMember assignment : assignments) {
            if (assignment.getProject() != null) {
                assignedProjects.add(assignment.getProject());
            }
        }

        return projectMapper.toDtoList(new ArrayList<>(assignedProjects));
    }

    /**
     * Get notification messages for a specific employee.
     * Returns a list of notification maps with project info.
     */
    public List<Map<String, Object>> getEmployeeNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Map<Long, Map<String, Object>> notificationsByProject = new HashMap<>();

        // 1) Assigned notifications (real assignments)
        List<TeamMember> assignments = teamMemberRepository.findByEmailIgnoreCaseWithProject(user.getEmail());
        for (TeamMember assignment : assignments) {
            Project project = assignment.getProject();
            if (project == null || project.getId() == null) continue;

            MatchingFallbackData fallbackData = extractFallbackDataFromLatestProjectMatching(project, user);

            Map<String, Object> notification = new HashMap<>();
            notification.put("projectId", project.getId());
            notification.put("projectName", project.getName());
            notification.put("projectDescription", project.getDescription());
            notification.put("managerName", project.getManager());
            notification.put("status", project.getStatus());
            notification.put("duration", project.getDuration());
            notification.put("score", fallbackData.score);
            notification.put("assigned", true);
            notification.put("message", "Vous avez ete assigne(e) au projet : " + project.getName());
            notification.put("date", java.time.LocalDateTime.now());
            notification.put("recommendedCourses", fallbackData.recommendedCourses);
            notification.put("missingSkills", fallbackData.missingSkills);

            notificationsByProject.put(project.getId(), notification);
        }

        // 2) Recommendation notifications from per-employee matching scores
        List<EmployeeProjectScore> scoreRows = new ArrayList<>();
        scoreRows.addAll(employeeProjectScoreRepository.findByEmployeeIdOrderByCreatedAtDesc("emp" + userId));
        scoreRows.addAll(employeeProjectScoreRepository.findByEmployeeIdOrderByCreatedAtDesc(String.valueOf(userId)));

        for (EmployeeProjectScore scoreRow : scoreRows) {
            Project project = scoreRow.getProject();
            if (project == null || project.getId() == null) continue;

            List<String> recommendedCourses = extractRecommendedCourses(scoreRow);
            List<String> missingSkills = extractMissingSkills(scoreRow);

            Map<String, Object> notification = notificationsByProject.get(project.getId());
            if (notification == null) {
                notification = new HashMap<>();
                notification.put("projectId", project.getId());
                notification.put("projectName", project.getName());
                notification.put("projectDescription", project.getDescription());
                notification.put("managerName", project.getManager());
                notification.put("status", project.getStatus());
                notification.put("duration", project.getDuration());
                notification.put("assigned", false);
                notification.put("message", "Recommendation de matching pour le projet : " + project.getName());
                notification.put("recommendedCourses", recommendedCourses);
                notification.put("missingSkills", missingSkills);
                notificationsByProject.put(project.getId(), notification);
            } else {
                if (!recommendedCourses.isEmpty()) {
                    notification.put("recommendedCourses", recommendedCourses);
                }
                if (!missingSkills.isEmpty()) {
                    notification.put("missingSkills", missingSkills);
                }
            }

            // Keep the most recent/available score and date
            notification.put("score", scoreRow.getOverallScore());
            notification.put("date", scoreRow.getCreatedAt());
        }

        List<Map<String, Object>> notifications = new ArrayList<>(notificationsByProject.values());
        notifications.sort((a, b) -> {
            Object da = a.get("date");
            Object db = b.get("date");
            if (da instanceof java.time.LocalDateTime && db instanceof java.time.LocalDateTime) {
                return ((java.time.LocalDateTime) db).compareTo((java.time.LocalDateTime) da);
            }
            return 0;
        });

        return notifications;
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

    // ===== NEW METHODS FOR EMPLOYEE SCORES =====

    /**
     * Get all employee scores for a project, sorted by overall score descending
     */
    public List<EmployeeProjectScore> getEmployeeScoresForProject(Long projectId) {
        return employeeProjectScoreRepository.findByProjectIdOrderByOverallScoreDesc(projectId);
    }

    /**
     * Get score for a specific employee and project
     */
    public EmployeeProjectScore getEmployeeScore(Long projectId, String employeeId) {
        return employeeProjectScoreRepository.findByProjectIdAndEmployeeId(projectId, employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Score not found for employee " + employeeId + " and project " + projectId));
    }

    /**
     * Get top N candidates for a project
     */
    public List<EmployeeProjectScore> getTopCandidatesForProject(Long projectId, int limit) {
        return employeeProjectScoreRepository.findTopScoresForProject(projectId, limit);
    }

    /**
     * Get all scores for a specific employee across all projects
     */
    public List<EmployeeProjectScore> getEmployeeAllScores(String employeeId) {
        return employeeProjectScoreRepository.findByEmployeeIdOrderByCreatedAtDesc(employeeId);
    }

    /**
     * Count total scores for a project
     */
    public long getEmployeeScoresCountForProject(Long projectId) {
        return employeeProjectScoreRepository.countByProjectId(projectId);
    }

    /**
     * Delete all scores for a project (useful when re-running matching)
     */
    public void deleteEmployeeScoresForProject(Long projectId) {
        employeeProjectScoreRepository.deleteByProjectId(projectId);
    }
}
