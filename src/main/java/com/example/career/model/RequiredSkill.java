package com.example.career.model;

import jakarta.persistence.*;

@Entity
public class RequiredSkill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String skillName;
    private String level;
    private Integer count;

    // taxonomy metadata (optional)
    private String domain;
    private String family;
    private String category;
    private String type;

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;

    public RequiredSkill() {}

    public RequiredSkill(String skillName, String level, Integer count) {
        this.skillName = skillName;
        this.level = level;
        this.count = count;
    }

    // convenience constructor with metadata
    public RequiredSkill(String skillName, String level, Integer count,
                         String domain, String family, String category, String type) {
        this.skillName = skillName;
        this.level = level;
        this.count = count;
        this.domain = domain;
        this.family = family;
        this.category = category;
        this.type = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getFamily() {
        return family;
    }

    public void setFamily(String family) {
        this.family = family;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
}
