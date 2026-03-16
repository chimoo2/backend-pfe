package com.example.career.repository;

import com.example.career.model.SkillCategoryRequirement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkillCategoryRequirementRepository extends JpaRepository<SkillCategoryRequirement, Long> {
    List<SkillCategoryRequirement> findByProjectId(Long projectId);
    void deleteByProjectId(Long projectId);
}
