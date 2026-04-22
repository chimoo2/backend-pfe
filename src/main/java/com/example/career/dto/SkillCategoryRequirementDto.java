package com.example.career.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for SkillCategoryRequirement
 * Used to transfer skill category/family/type requirements between frontend and backend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SkillCategoryRequirementDto {
    private Long id;
    private String filterType; // "family", "category", "type"
    private String filterValue; // e.g., "Backend", "Data Science"
    private String description;
    private Integer minCriticality;
}
