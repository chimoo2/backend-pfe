package com.example.career.controller;

import com.example.career.model.EmployeeProjectScore;
import com.example.career.model.User;
import com.example.career.repository.EmployeeProjectScoreRepository;
import com.example.career.repository.ProjectRepository;
import com.example.career.repository.UserRepository;
import com.example.career.repository.UserSkillRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Public endpoint returning aggregate platform statistics for the landing page.
 * No authentication required — see SecurityConfig.
 */
@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final EmployeeProjectScoreRepository scoreRepo;
    private final ProjectRepository projectRepo;
    private final UserRepository userRepo;
    private final UserSkillRepository userSkillRepo;

    public StatsController(EmployeeProjectScoreRepository scoreRepo,
                           ProjectRepository projectRepo,
                           UserRepository userRepo,
                           UserSkillRepository userSkillRepo) {
        this.scoreRepo = scoreRepo;
        this.projectRepo = projectRepo;
        this.userRepo = userRepo;
        this.userSkillRepo = userSkillRepo;
    }

    @GetMapping("/landing")
    public Map<String, Object> landingStats() {
        List<EmployeeProjectScore> allScores = scoreRepo.findAll();
        long totalProjects = projectRepo.count();
        long totalUsers = userRepo.count();

        // --- 1. Employee readiness score: average overallScore across all matchings ---
        double avgScore = allScores.stream()
                .filter(s -> s.getOverallScore() != null)
                .mapToDouble(s -> normalize(s.getOverallScore()))
                .average()
                .orElse(0.0);
        int readiness = (int) Math.round(avgScore * 100);

        // --- 2. Skill coverage: % of users that have at least one skill registered ---
        long usersWithSkills = 0;
        List<User> users = userRepo.findAll();
        for (User u : users) {
            if (!userSkillRepo.findByUser(u).isEmpty()) usersWithSkills++;
        }
        int skillCoverage = totalUsers == 0 ? 0 : (int) Math.round((usersWithSkills * 100.0) / totalUsers);

        // --- 3. Project fit accuracy: average TOP score per project ---
        Map<Long, Double> bestPerProject = new HashMap<>();
        for (EmployeeProjectScore s : allScores) {
            if (s.getProject() == null || s.getOverallScore() == null) continue;
            Long pid = s.getProject().getId();
            double v = normalize(s.getOverallScore());
            bestPerProject.merge(pid, v, Math::max);
        }
        double avgTop = bestPerProject.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        int projectFit = (int) Math.round(avgTop * 100);

        // --- 4. Staffing leverage: average number of analyzed candidates per project (≥1) ---
        Set<Long> analyzedProjects = new HashSet<>(bestPerProject.keySet());
        double staffingX = analyzedProjects.isEmpty()
                ? 0.0
                : (double) allScores.size() / analyzedProjects.size();
        // Round to one decimal
        double staffingRounded = Math.round(staffingX * 10.0) / 10.0;

        Map<String, Object> out = new HashMap<>();
        out.put("readiness", readiness);
        out.put("skillCoverage", skillCoverage);
        out.put("projectFit", projectFit);
        out.put("staffingMultiplier", staffingRounded);
        out.put("totalProjects", totalProjects);
        out.put("totalUsers", totalUsers);
        out.put("totalMatchings", allScores.size());
        out.put("analyzedProjects", analyzedProjects.size());
        return out;
    }

    /** Accept both 0..1 and 0..100 score formats; return 0..1. */
    private static double normalize(double v) {
        if (v > 1.0) v = v / 100.0;
        if (v < 0.0) v = 0.0;
        if (v > 1.0) v = 1.0;
        return v;
    }
}
