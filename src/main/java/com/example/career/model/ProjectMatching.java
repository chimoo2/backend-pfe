package com.example.career.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
public class ProjectMatching {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnore
    private Project project;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private Integer totalCandidates;
    private Integer skillCount;
    private Double matchingScore;
    private String message;

    @Lob
    @Column(columnDefinition = "text")
    private String matchesData; // JSON string containing the matches

    // constructors
    public ProjectMatching() {
        this.createdAt = LocalDateTime.now();
    }

    public ProjectMatching(Project project, Integer totalCandidates, Integer skillCount,
                          Double matchingScore, String message, String matchesData) {
        this.project = project;
        this.totalCandidates = totalCandidates;
        this.skillCount = skillCount;
        this.matchingScore = matchingScore;
        this.message = message;
        this.matchesData = matchesData;
        this.createdAt = LocalDateTime.now();
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getTotalCandidates() {
        return totalCandidates;
    }

    public void setTotalCandidates(Integer totalCandidates) {
        this.totalCandidates = totalCandidates;
    }

    public Integer getSkillCount() {
        return skillCount;
    }

    public void setSkillCount(Integer skillCount) {
        this.skillCount = skillCount;
    }

    public Double getMatchingScore() {
        return matchingScore;
    }

    public void setMatchingScore(Double matchingScore) {
        this.matchingScore = matchingScore;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMatchesData() {
        return matchesData;
    }

    public void setMatchesData(String matchesData) {
        this.matchesData = matchesData;
    }
}