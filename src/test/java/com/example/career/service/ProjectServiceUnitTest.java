package com.example.career.service;

import com.example.career.dto.ProjectDto;
import com.example.career.dto.RequiredSkillDto;
import com.example.career.dto.SkillCategoryRequirementDto;
import com.example.career.service.ProjectService;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class ProjectServiceUnitTest {

    @Test
    public void convertEmployees_shouldNormalizeMidLevel() {
        ProjectService service = new ProjectService();
        List<Map<String,Object>> backendData = new ArrayList<>();
        Map<String,Object> emp = new HashMap<>();
        emp.put("id", 1);
        emp.put("name", "Test");
        List<Map<String,Object>> skills = new ArrayList<>();
        Map<String,Object> skill = new HashMap<>();
        skill.put("name", "SomeSkill");
        skill.put("level", "Mid");
        skill.put("yearsExperience", 2);
        skills.add(skill);
        emp.put("skills", skills);
        backendData.add(emp);

        List<Map<String,Object>> converted = service.convertEmployeesForMicroservice(backendData);
        assertEquals(1, converted.size());
        Map<String,Object> cEmp = converted.get(0);
        assertEquals("emp1", cEmp.get("id"));
        List<Map<String,Object>> cSkills = (List<Map<String,Object>>) cEmp.get("skills");
        assertFalse(cSkills.isEmpty());
        assertEquals("Intermediate", cSkills.get(0).get("level"));
    }

    @Test
    public void convertProjectForMicroservice_includesMetadataAndCategories() {
        ProjectService service = new ProjectService();
        ProjectDto dto = new ProjectDto();
        dto.setId(5L);
        dto.setName("TestProj");
        RequiredSkillDto skillDto = new RequiredSkillDto(
    null,
    "Java",
    3,
    "Software Engineering",
    "Backend",
    "Programming Language",
    "Technical"
);
        dto.setRequiredSkills(Arrays.asList(skillDto));
        SkillCategoryRequirementDto catReq = new SkillCategoryRequirementDto(null, "family", "Backend", "desc", 4);
        dto.setCategoryRequirements(Arrays.asList(catReq));

        Map<String,Object> conv = service.convertProjectForMicroservice(dto);
        assertEquals("5", conv.get("id"));
        assertEquals("TestProj", conv.get("name"));
        List<Map<String,Object>> reqs = (List<Map<String,Object>>) conv.get("requirements");
        assertTrue(reqs.stream().anyMatch(r -> "React".equals(r.get("skill_name")) && "Frontend".equals(r.get("family"))));
        assertTrue(reqs.stream().anyMatch(r -> "Backend".equals(r.get("family")) && !r.containsKey("skill_name")));
    }
}
