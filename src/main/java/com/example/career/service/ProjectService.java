package com.example.career.service;

import com.example.career.dto.ProjectDto;
import com.example.career.mapper.ProjectMapper;
import com.example.career.model.Project;
import com.example.career.model.RequiredSkill;
import com.example.career.model.SkillCategoryRequirement;
import com.example.career.repository.ProjectRepository;
import com.example.career.repository.RequiredSkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.example.career.model.ProjectMatching;
import com.example.career.repository.ProjectMatchingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.io.InputStream;
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

    // Jackson object mapper for reading JSON files and serializing payloads
    @Autowired
    private ObjectMapper objectMapper;

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
                req.setCount(crDto.getCount());
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
                req.setCount(crDto.getCount());
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
            // assume microservice is running locally on port 8001 under /api/v1 prefix
            String url = "http://localhost:8001/api/v1/match/inline";
            MatchingResult result = rest.postForObject(url, payload, MatchingResult.class);

            // Normalize/massage the response for our frontend needs
            normalizeMatchingResult(result);

            // Persist the matching result
            saveMatchingResult(project, result);

            return result;
        } catch (Exception ex) {
            // fallback stub if microservice is unreachable
            int skillCount = project.getRequiredSkills().size();
            double matchingScore = Math.min(100, skillCount * 10);
            MatchingResult fallback = new MatchingResult();
            fallback.setProjectId(projectId);
            fallback.setProjectName(project.getName());
            fallback.setSkillCount(skillCount);
            fallback.setMatchingScore(matchingScore);
            fallback.setMessage("Fallback stub (microservice error): " + ex.getMessage());

            // Normalize/massage the fallback response as well
            normalizeMatchingResult(fallback);

            // Persist the fallback result too
            saveMatchingResult(project, fallback);

            return fallback;
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
            if (matchDetails != null) {
                match.put("direct_skill_score", matchDetails.get("direct_score"));
                match.put("related_skill_score", matchDetails.get("related_score"));

                // Derive a semantic score as (matched_semantic_skills / total requirements)
                double semanticScore = 0.0;
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
     * Load employees from the embedded JSON file and convert to microservice format.
     */
    private List<Map<String,Object>> loadEmployeeData() {
        try {
            ClassPathResource res = new ClassPathResource("data/employees.json");
            InputStream is = res.getInputStream();
            List<Map<String,Object>> backendEmployees = objectMapper.readValue(is, new TypeReference<List<Map<String,Object>>>(){});
            return convertEmployeesForMicroservice(backendEmployees);
        } catch (Exception e) {
            return List.of();
        }
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
                    String levelStr = lvl == null ? "" : lvl.toString();
                    if (levelStr.equalsIgnoreCase("mid") || levelStr.equalsIgnoreCase("middle")) {
                        levelStr = "Intermediate";
                    }
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
                if (cr.getCount() != null) r.put("count", cr.getCount());
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
    public static class MatchingResult {
        @JsonAlias("project_id")
        private Long projectId;
        @JsonAlias("project_name")
        private String projectName;
        private int skillCount;
        private double matchingScore;
        private String message;

        // new fields coming from microservice response
        @JsonAlias("total_candidates")
        private Integer totalCandidates;
        private List<Map<String,Object>> matches;

        public Long getProjectId() {
            return projectId;
        }
        
        public void setProjectId(Long projectId) {
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
