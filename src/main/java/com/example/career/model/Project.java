package com.example.career.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
//Cette classe correspond à une table dans la base de données.
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String manager;
    private LocalDate startDate;
    private LocalDate endDate;
    private String duration;
    private String status;
    //Permet d’avoir un texte long (2000 caractères)

    @Column(length = 2000)
    private String description;

    private Integer count; // nombre de personnes nécessaires pour le projet

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    //Un projet peut avoir plusieurs compétences requises
    private List<RequiredSkill> requiredSkills = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    //Un projet peut avoir plusieurs exigences de catégories de compétences
    private List<SkillCategoryRequirement> categoryRequirements = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    //Un projet peut avoir plusieurs résultats de matching
    private List<ProjectMatching> matchings = new ArrayList<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<TeamMember> teamMembers = new ArrayList<>();

    // constructors
    public Project() {}

    public Project(String name, String manager, LocalDate startDate, String duration, String status, String description) {
        this.name = name;
        this.manager = manager;
        this.startDate = startDate;
        this.duration = duration;
        this.status = status;
        this.description = description;
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public List<RequiredSkill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<RequiredSkill> requiredSkills) {
        this.requiredSkills = requiredSkills;
        requiredSkills.forEach(s -> s.setProject(this));
    }

    // helper methods
    public void addRequiredSkill(RequiredSkill skill) {
        requiredSkills.add(skill);
        skill.setProject(this);
    }

    public void removeRequiredSkill(RequiredSkill skill) {
        requiredSkills.remove(skill);
        skill.setProject(null);
    }

    public List<SkillCategoryRequirement> getCategoryRequirements() {
        return categoryRequirements;
    }

    public void setCategoryRequirements(List<SkillCategoryRequirement> categoryRequirements) {
        this.categoryRequirements = categoryRequirements;
        categoryRequirements.forEach(cr -> cr.setProject(this));
    }

    public void addCategoryRequirement(SkillCategoryRequirement requirement) {
        categoryRequirements.add(requirement);
        requirement.setProject(this);
    }

    public void removeCategoryRequirement(SkillCategoryRequirement requirement) {
        categoryRequirements.remove(requirement);
    }

    public List<ProjectMatching> getMatchings() {
        return matchings;
    }

    public void setMatchings(List<ProjectMatching> matchings) {
        this.matchings = matchings;
        matchings.forEach(m -> m.setProject(this));
    }

    public void addProjectMatching(ProjectMatching matching) {
        matchings.add(matching);
        matching.setProject(this);
    }

    public void removeProjectMatching(ProjectMatching matching) {
        matchings.remove(matching);
        matching.setProject(null);
    }

    public List<TeamMember> getTeamMembers() {
        return teamMembers;
    }

    public void setTeamMembers(List<TeamMember> teamMembers) {
        this.teamMembers = teamMembers;
        teamMembers.forEach(member -> member.setProject(this));
    }

    public void addTeamMember(TeamMember teamMember) {
        teamMembers.add(teamMember);
        teamMember.setProject(this);
    }

    public void removeTeamMember(TeamMember teamMember) {
        teamMembers.remove(teamMember);
        teamMember.setProject(null);
    }
}
