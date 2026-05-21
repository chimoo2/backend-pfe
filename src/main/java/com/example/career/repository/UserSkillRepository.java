package com.example.career.repository;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;

import com.example.career.model.UserSkill;
import com.example.career.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    List<UserSkill> findByUser(User user);

    Optional<UserSkill> findByUserAndSkillName(User user, String skillName);

    @Transactional
    void deleteByUserAndSkillName(User user, String skillName);
}
