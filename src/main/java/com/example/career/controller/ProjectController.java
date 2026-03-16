package com.example.career.controller;

import com.example.career.dto.ProjectDto;
import com.example.career.service.ProjectService;
import com.example.career.model.ProjectMatching;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
// allow both dev ports, in case Vite uses 5173 or 5175
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5175"})
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * GET /api/projects - Get all projects
     */
    @GetMapping
    public ResponseEntity<List<ProjectDto>> getAllProjects() {
        List<ProjectDto> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }

    /**
     * GET /api/projects/manager/{managerEmail} - Get projects for a specific manager
     */
    @GetMapping("/manager/{managerEmail}")
    public ResponseEntity<List<ProjectDto>> getProjectsByManager(@PathVariable String managerEmail) {
        List<ProjectDto> projects = projectService.getProjectsByManager(managerEmail);
        return ResponseEntity.ok(projects);
    }

    /**
     * GET /api/projects/{id} - Get project by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> getProjectById(@PathVariable Long id) {
        ProjectDto project = projectService.getProjectById(id);
        if (project != null) {
            return ResponseEntity.ok(project);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * POST /api/projects - Create a new project
     */
    @PostMapping
    public ResponseEntity<?> createProject(@RequestBody ProjectDto projectDto) {
        try {
            ProjectDto createdProject = projectService.createProject(projectDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
        } catch (IllegalArgumentException e) {
            // return message so frontend can display reason
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * PUT /api/projects/{id} - Update a project
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProject(@PathVariable Long id, @RequestBody ProjectDto projectDto) {
        try {
            ProjectDto updatedProject = projectService.updateProject(id, projectDto);
            if (updatedProject != null) {
                return ResponseEntity.ok(updatedProject);
            }
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * DELETE /api/projects/{id} - Delete a project
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        boolean deleted = projectService.deleteProject(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/projects/{id}/matching - Get matching candidates for a project (stub)
     */
    @GetMapping("/{id}/matching")
    public ResponseEntity<ProjectService.MatchingResult> getMatchingCandidates(@PathVariable Long id) {
        ProjectService.MatchingResult result = projectService.getMatchingCandidates(id);
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/projects/{id}/matching/history - Get matching history for a project
     */
    @GetMapping("/{id}/matching/history")
    public ResponseEntity<List<ProjectMatching>> getMatchingHistory(@PathVariable Long id) {
        List<ProjectMatching> history = projectService.getMatchingHistory(id);
        return ResponseEntity.ok(history);
    }

    /**
     * GET /api/projects/{id}/matching/latest - Get latest matching result for a project
     */
    @GetMapping("/{id}/matching/latest")
    public ResponseEntity<ProjectMatching> getLatestMatching(@PathVariable Long id) {
        ProjectMatching latest = projectService.getLatestMatching(id);
        if (latest != null) {
            return ResponseEntity.ok(latest);
        }
        return ResponseEntity.notFound().build();
    }
}
