package com.example.career.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import com.example.career.dto.SkillCategoryRequirementDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String manager;
    private List<RequiredSkillDto> requiredSkills;
    private List<SkillCategoryRequirementDto> categoryRequirements;
}
