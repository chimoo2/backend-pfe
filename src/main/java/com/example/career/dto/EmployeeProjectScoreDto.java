package com.example.career.dto;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonAlias;

public class EmployeeProjectScoreDto {
    private Long id;
    private Long projectId;
    private String employeeId;
    private String employeeName;
    private Double overallScore;
    private Double directSkillScore;
    private Double relatedSkillScore;
    private Double semanticSkillScore;
    private String matchedSkills;
    private String missingSkills;
    private LocalDateTime createdAt;

    // Constructors
    public EmployeeProjectScoreDto() {}

    public EmployeeProjectScoreDto(Long id, Long projectId, String employeeId, String employeeName,
                                   Double overallScore, Double directSkillScore, Double relatedSkillScore,
                                   Double semanticSkillScore, LocalDateTime createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.overallScore = overallScore;
        this.directSkillScore = directSkillScore;
        this.relatedSkillScore = relatedSkillScore;
        this.semanticSkillScore = semanticSkillScore;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "EmployeeProjectScoreDto{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", employeeId='" + employeeId + '\'' +
                ", employeeName='" + employeeName + '\'' +
                ", overallScore=" + overallScore +
                ", directSkillScore=" + directSkillScore +
                ", relatedSkillScore=" + relatedSkillScore +
                ", semanticSkillScore=" + semanticSkillScore +
                ", createdAt=" + createdAt +
                '}';
    }
}
