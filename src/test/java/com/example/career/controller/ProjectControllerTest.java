package com.example.career.controller;

import com.example.career.dto.ProjectDto;
import com.example.career.dto.RequiredSkillDto;
import com.example.career.dto.SkillCategoryRequirementDto; // added missing import
import com.example.career.service.ProjectService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @Test
    public void testGetAllProjects() throws Exception {
        // Arrange
        ProjectDto project1 = new ProjectDto();
        project1.setId(1L);
        project1.setName("Project 1");
        project1.setManager("Manager 1");
        project1.setStartDate(LocalDate.now());
        project1.setStatus("active");
        project1.setDescription("Description 1");

        ProjectDto project2 = new ProjectDto();
        project2.setId(2L);
        project2.setName("Project 2");
        project2.setManager("Manager 2");
        project2.setStartDate(LocalDate.now());
        project2.setStatus("active");
        project2.setDescription("Description 2");

        List<ProjectDto> projects = Arrays.asList(project1, project2);

        when(projectService.getAllProjects()).thenReturn(projects);

        // Act & Assert
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));

        verify(projectService, times(1)).getAllProjects();
    }

    @Test
    public void testGetProjectById() throws Exception {
        // Arrange
        ProjectDto project = new ProjectDto();
        project.setId(1L);
        project.setName("Project 1");
        project.setManager("Manager 1");
        project.setStartDate(LocalDate.now());
        project.setStatus("active");
        project.setDescription("Description 1");

        when(projectService.getProjectById(1L)).thenReturn(project);

        // Act & Assert
        mockMvc.perform(get("/api/projects/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Project 1"));

        verify(projectService, times(1)).getProjectById(1L);
    }

    @Test
    public void testCreateProject() throws Exception {
        // Arrange
        ProjectDto projectDto = new ProjectDto();
        projectDto.setName("New Project");
        projectDto.setManager("New Manager");
        projectDto.setStartDate(LocalDate.now());
        projectDto.setStatus("active");
        projectDto.setDescription("New Description");
        projectDto.setRequiredSkills(Arrays.asList(
        new RequiredSkillDto(
                null,
                "Java",
                3,
                "Software Engineering",
                "Backend",
                "Programming Language",
                "Technical"
        ),

        new RequiredSkillDto(
                null,
                "Spring",
                2,
                "Software Engineering",
                "Backend",
                "Framework",
                "Technical"
        )
));
        projectDto.setCategoryRequirements(Arrays.asList(
                new SkillCategoryRequirementDto(null, "family", "Backend", "Need backend", 3)
        ));

        ProjectDto createdProject = new ProjectDto();
        createdProject.setId(1L);
        createdProject.setName("New Project");
        createdProject.setManager("New Manager");
        createdProject.setStartDate(LocalDate.now());
        createdProject.setStatus("active");
        createdProject.setDescription("New Description");
        createdProject.setRequiredSkills(projectDto.getRequiredSkills());
        createdProject.setCategoryRequirements(projectDto.getCategoryRequirements());

        when(projectService.createProject(any(ProjectDto.class))).thenReturn(createdProject);

        // Act & Assert
        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("New Project"))
                .andExpect(jsonPath("$.categoryRequirements[0].filterValue").value("Backend"));

        verify(projectService, times(1)).createProject(any(ProjectDto.class));
    }

    @Test
    public void testUpdateProject() throws Exception {
        // Arrange
        ProjectDto updateDto = new ProjectDto();
        updateDto.setName("Updated Project");
        updateDto.setManager("Updated Manager");
        updateDto.setStartDate(LocalDate.now());
        updateDto.setStatus("completed");
        updateDto.setDescription("Updated Description");

        ProjectDto updatedProject = new ProjectDto();
        updatedProject.setId(1L);
        updatedProject.setName("Updated Project");
        updatedProject.setManager("Updated Manager");
        updatedProject.setStartDate(LocalDate.now());
        updatedProject.setStatus("completed");
        updatedProject.setDescription("Updated Description");

        when(projectService.updateProject(eq(1L), any(ProjectDto.class))).thenReturn(updatedProject);

        // Act & Assert
        mockMvc.perform(put("/api/projects/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Project"));

        verify(projectService, times(1)).updateProject(eq(1L), any(ProjectDto.class));
    }

    @Test
    public void testDeleteProject() throws Exception {
        // Arrange
        when(projectService.deleteProject(1L)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(delete("/api/projects/1"))
                .andExpect(status().isNoContent());

        verify(projectService, times(1)).deleteProject(1L);
    }

    @Test
    public void testGetMatchingCandidates() throws Exception {
        // Arrange
        ProjectService.MatchingResult result = new ProjectService.MatchingResult();
        result.setProjectId("1");
        result.setProjectName("Project 1");
        result.setSkillCount(2);
        result.setMatchingScore(20.0);
        result.setMessage("Stub: Matching with AI microservice to be integrated");

        when(projectService.getMatchingCandidates(1L)).thenReturn(result);

        // Act & Assert
        mockMvc.perform(get("/api/projects/1/matching"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectId").value(1))
                .andExpect(jsonPath("$.skillCount").value(2));

        verify(projectService, times(1)).getMatchingCandidates(1L);
    }
}
