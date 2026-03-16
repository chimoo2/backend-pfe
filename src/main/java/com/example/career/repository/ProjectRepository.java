package com.example.career.repository;

import com.example.career.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findByManager(String manager);
    List<Project> findByStatus(String status);
}
