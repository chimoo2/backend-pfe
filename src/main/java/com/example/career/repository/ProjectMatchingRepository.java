package com.example.career.repository;

import com.example.career.model.ProjectMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMatchingRepository extends JpaRepository<ProjectMatching, Long> {
    List<ProjectMatching> findByProjectIdOrderByCreatedAtDesc(Long projectId);
    List<ProjectMatching> findTop10ByProjectIdOrderByCreatedAtDesc(Long projectId);
}