package com.example.career.mapper;

import com.example.career.dto.ProjectDto;
import com.example.career.dto.RequiredSkillDto;
import com.example.career.model.Project;
import com.example.career.model.RequiredSkill;
import com.example.career.model.SkillCategoryRequirement;
import com.example.career.dto.SkillCategoryRequirementDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectMapper {

    public ProjectDto toDto(Project project) {
        if (project == null) {
            return null;
        }

        ProjectDto dto = new ProjectDto();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setManager(project.getManager());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setStatus(project.getStatus());
        dto.setDescription(project.getDescription());

        if (project.getRequiredSkills() != null) {
            List<RequiredSkillDto> skillDtos = project.getRequiredSkills().stream()
                    .map(this::skillToDto)
                    .collect(Collectors.toList());
            dto.setRequiredSkills(skillDtos);
        }
        if (project.getCategoryRequirements() != null) {
            List<SkillCategoryRequirementDto> reqs = project.getCategoryRequirements().stream()
                    .map(this::categoryToDto)
                    .collect(Collectors.toList());
            dto.setCategoryRequirements(reqs);
        }

        return dto;
    }

    public Project toEntity(ProjectDto dto) {
        if (dto == null) {
            return null;
        }

        Project project = new Project();
        project.setId(dto.getId());
        project.setName(dto.getName());
        project.setManager(dto.getManager());
        project.setStartDate(dto.getStartDate());
        project.setEndDate(dto.getEndDate());
        project.setStatus(dto.getStatus());
        project.setDescription(dto.getDescription());

        if (dto.getRequiredSkills() != null) {
            List<RequiredSkill> skills = dto.getRequiredSkills().stream()
                    .map(this::skillToEntity)
                    .collect(Collectors.toList());
            project.setRequiredSkills(skills);
        }
        if (dto.getCategoryRequirements() != null) {
            List<SkillCategoryRequirement> reqs = dto.getCategoryRequirements().stream()
                    .map(this::categoryToEntity)
                    .collect(Collectors.toList());
            project.setCategoryRequirements(reqs);
        }

        return project;
    }

    public RequiredSkillDto skillToDto(RequiredSkill skill) {
        if (skill == null) {
            return null;
        }

        RequiredSkillDto dto = new RequiredSkillDto();
        dto.setId(skill.getId());
        dto.setSkillName(skill.getSkillName());
        dto.setLevel(skill.getLevel());
        dto.setCount(skill.getCount());
        dto.setDomain(skill.getDomain());
        dto.setFamily(skill.getFamily());
        dto.setCategory(skill.getCategory());
        dto.setType(skill.getType());

        return dto;
    }

    public RequiredSkill skillToEntity(RequiredSkillDto dto) {
        if (dto == null) {
            return null;
        }

        RequiredSkill skill = new RequiredSkill();
        skill.setId(dto.getId());
        skill.setSkillName(dto.getSkillName());
        skill.setLevel(dto.getLevel());
        skill.setCount(dto.getCount());
        skill.setDomain(dto.getDomain());
        skill.setFamily(dto.getFamily());
        skill.setCategory(dto.getCategory());
        skill.setType(dto.getType());

        return skill;
    }

    public List<ProjectDto> toDtoList(List<Project> projects) {
        return projects.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // helpers for category requirements mapping
    public SkillCategoryRequirementDto categoryToDto(SkillCategoryRequirement req) {
        if (req == null) return null;
        SkillCategoryRequirementDto dto = new SkillCategoryRequirementDto();
        dto.setId(req.getId());
        dto.setFilterType(req.getFilterType());
        dto.setFilterValue(req.getFilterValue());
        dto.setDescription(req.getDescription());
        dto.setMinCriticality(req.getMinCriticality());
        dto.setCount(req.getCount());
        return dto;
    }

    public SkillCategoryRequirement categoryToEntity(SkillCategoryRequirementDto dto) {
        if (dto == null) return null;
        SkillCategoryRequirement req = new SkillCategoryRequirement();
        req.setId(dto.getId());
        req.setFilterType(dto.getFilterType());
        req.setFilterValue(dto.getFilterValue());
        req.setDescription(dto.getDescription());
        req.setMinCriticality(dto.getMinCriticality());
        req.setCount(dto.getCount());
        return req;
    }
}
