package com.example.career.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

/**
 * SkillTaxonomy - Represents a skill from the internal taxonomy
 * Maps to internal_skills_taxonomy.json from the AI microservice
 */
@Entity
@Table(name = "skill_taxonomy")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillTaxonomy {

    @Id
    private String skillName; // "Java", "Python", "React", etc.

    private String domain;      // "Software Engineering", "Data & AI", "Infrastructure"
    private String family;      // "Backend", "Frontend", "DevOps", "Data Science"
    private String category;    // "Programming Language", "Framework", "Library"
    private String type;        // "Technical", "Soft Skill", "Strategic"
    private Integer criticality; // 1-5 scale of importance

    @Override
    public String toString() {
        return String.format("%s (%s - %s)", skillName, family, category);
    }
}
