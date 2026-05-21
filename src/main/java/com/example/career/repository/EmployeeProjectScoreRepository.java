package com.example.career.repository;

import com.example.career.model.EmployeeProjectScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeProjectScoreRepository extends JpaRepository<EmployeeProjectScore, Long> {
    
    /**
     * Find all scores for a specific project
     */
    List<EmployeeProjectScore> findByProjectIdOrderByOverallScoreDesc(Long projectId);

    /**
     * Find scores for a specific project with pagination ordering
     */
    List<EmployeeProjectScore> findByProjectIdOrderByCreatedAtDesc(Long projectId);

    /**
     * Find score for a specific employee and project
     */
    Optional<EmployeeProjectScore> findByProjectIdAndEmployeeId(Long projectId, String employeeId);

    /**
     * Find all scores for a specific employee across all projects
     */
    List<EmployeeProjectScore> findByEmployeeIdOrderByCreatedAtDesc(String employeeId);

    /**
     * Find top N scores for a project
     */
    @Query("SELECT e FROM EmployeeProjectScore e WHERE e.project.id = :projectId ORDER BY e.overallScore DESC LIMIT :limit")
    List<EmployeeProjectScore> findTopScoresForProject(@Param("projectId") Long projectId, @Param("limit") int limit);

    /**
     * Find all scores created after a specific date for a project
     */
    @Query("SELECT e FROM EmployeeProjectScore e WHERE e.project.id = :projectId AND e.createdAt >= :date ORDER BY e.createdAt DESC")
    List<EmployeeProjectScore> findScoresForProjectSinceDate(@Param("projectId") Long projectId, @Param("date") java.time.LocalDateTime date);

    /**
     * Delete all scores for a project
     */
    void deleteByProjectId(Long projectId);

    /**
     * Count scores for a project
     */
    long countByProjectId(Long projectId);
}
