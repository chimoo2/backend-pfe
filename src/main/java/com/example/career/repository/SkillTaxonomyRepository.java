package com.example.career.repository;

import com.example.career.model.SkillTaxonomy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SkillTaxonomyRepository extends JpaRepository<SkillTaxonomy, String> {

    /**
     * Find all skills by family (e.g., "Backend", "Frontend", "DevOps")
     */
    List<SkillTaxonomy> findByFamily(String family);

    /**
     * Find all skills by domain (e.g., "Software Engineering", "Data & AI")
     */
    List<SkillTaxonomy> findByDomain(String domain);

    /**
     * Find all unique families in the taxonomy
     */
    @Query("SELECT DISTINCT t.family FROM SkillTaxonomy t ORDER BY t.family")
    List<String> findDistinctFamilies();

    /**
     * Find all unique domains in the taxonomy
     */
    @Query("SELECT DISTINCT t.domain FROM SkillTaxonomy t ORDER BY t.domain")
    List<String> findDistinctDomains();

    /**
     * Find skills by multiple families
     */
    @Query("SELECT t FROM SkillTaxonomy t WHERE t.family IN :families ORDER BY t.criticality DESC, t.skillName")
    List<SkillTaxonomy> findByFamilies(List<String> families);

    /**
     * Find skills by type (e.g., "Technical", "Soft Skill")
     */
    List<SkillTaxonomy> findByType(String type);

    /**
     * Get skills with criticality >= threshold
     */
    @Query("SELECT t FROM SkillTaxonomy t WHERE t.criticality >= :minCriticality ORDER BY t.criticality DESC")
    List<SkillTaxonomy> findByCriticalityGreaterOrEqual(Integer minCriticality);
}
