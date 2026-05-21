package com.example.career.mapper;

import com.example.career.dto.ProjectDto;
import com.example.career.dto.RequiredSkillDto;
import com.example.career.dto.TeamMemberDto;
import com.example.career.model.Project;
import com.example.career.model.RequiredSkill;
import com.example.career.model.SkillCategoryRequirement;
import com.example.career.model.TeamMember;
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
        dto.setCount(project.getCount());
        dto.setDuration(project.getDuration());

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
        if (project.getTeamMembers() != null) {
            List<TeamMemberDto> teamMembers = project.getTeamMembers().stream()
                .map(this::teamMemberToDto)
                .collect(Collectors.toList());
            dto.setTeamMembers(teamMembers);
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
        project.setCount(dto.getCount());
        project.setDuration(dto.getDuration());

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
        if (dto.getTeamMembers() != null) {
            List<TeamMember> teamMembers = dto.getTeamMembers().stream()
                .map(this::teamMemberToEntity)
                .collect(Collectors.toList());
            project.setTeamMembers(teamMembers);
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
        dto.setCriticality(skill.getCriticality());
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
        skill.setCriticality(dto.getCriticality());
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
        return req;
    }

    public TeamMemberDto teamMemberToDto(TeamMember member) {
        if (member == null) return null;
        TeamMemberDto dto = new TeamMemberDto();
        dto.setId(member.getId());
        dto.setFirstName(member.getFirstName());
        dto.setLastName(member.getLastName());
        dto.setEmail(member.getEmail());
        dto.setRole(member.getRole());
        return dto;
    }

    public TeamMember teamMemberToEntity(TeamMemberDto dto) {
        if (dto == null) return null;
        TeamMember member = new TeamMember();
        member.setId(dto.getId());
        member.setFirstName(dto.getFirstName());
        member.setLastName(dto.getLastName());
        member.setEmail(dto.getEmail());
        member.setRole(dto.getRole());
        return member;
    }
}
