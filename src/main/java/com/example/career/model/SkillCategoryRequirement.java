package com.example.career.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * SkillCategoryRequirement - Represents a requirement by skill family/category/type
 * instead of specific skill names. More flexible for project staffing.
 * Example: "Need a Backend expert" vs "Need Python Senior"
 */
@Entity
@Table(name = "skill_category_requirement")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCategoryRequirement {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    // One of: family, category, or type
    @Column(nullable = false)
    private String filterType; // "family", "category", "type"
    
    @Column(nullable = false)
    private String filterValue; // e.g., "Backend", "Data Science", "Technical"
    
    @Column
    private String description; // e.g., "Backend expertise required"
    
    @Column
    private Integer minCriticality; // minimum skill criticality level (1-5)
    
    @Column
    private Integer count; // how many people with this skill needed
}
