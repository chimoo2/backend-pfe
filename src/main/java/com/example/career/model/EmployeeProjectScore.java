package com.example.career.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_project_score", indexes = {
    @Index(name = "idx_project_employee", columnList = "project_id, employee_id"),
    @Index(name = "idx_project_created", columnList = "project_id, created_at")
})
public class EmployeeProjectScore {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String employeeId;

    @Column(nullable = false)
    private String employeeName;

    @Column(nullable = false)
    private Double overallScore;

    private Double directSkillScore;

    private Double relatedSkillScore;

    private Double semanticSkillScore;

    @Column(length = 3000)
    private String matchedSkills;

    @Column(length = 3000)
    private String missingSkills;

    @Column(columnDefinition = "text")
    private String fullMatchDetails;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Constructors
    public EmployeeProjectScore() {
        this.createdAt = LocalDateTime.now();
    }

    public EmployeeProjectScore(Project project, String employeeId, String employeeName, Double overallScore) {
        this.project = project;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.overallScore = overallScore;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
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

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public Double getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Double overallScore) {
        this.overallScore = overallScore;
    }

    public Double getDirectSkillScore() {
        return directSkillScore;
    }

    public void setDirectSkillScore(Double directSkillScore) {
        this.directSkillScore = directSkillScore;
    }

    public Double getRelatedSkillScore() {
        return relatedSkillScore;
    }

    public void setRelatedSkillScore(Double relatedSkillScore) {
        this.relatedSkillScore = relatedSkillScore;
    }

    public Double getSemanticSkillScore() {
        return semanticSkillScore;
    }

    public void setSemanticSkillScore(Double semanticSkillScore) {
        this.semanticSkillScore = semanticSkillScore;
    }

    public String getMatchedSkills() {
        return matchedSkills;
    }

    public void setMatchedSkills(String matchedSkills) {
        this.matchedSkills = matchedSkills;
    }

    public String getMissingSkills() {
        return missingSkills;
    }

    public void setMissingSkills(String missingSkills) {
        this.missingSkills = missingSkills;
    }

    public String getFullMatchDetails() {
        return fullMatchDetails;
    }

    public void setFullMatchDetails(String fullMatchDetails) {
        this.fullMatchDetails = fullMatchDetails;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
