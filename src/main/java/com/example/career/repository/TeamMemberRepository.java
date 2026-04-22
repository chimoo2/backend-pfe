package com.example.career.repository;

import com.example.career.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    @Query("SELECT tm FROM TeamMember tm JOIN FETCH tm.project WHERE LOWER(tm.email) = LOWER(:email)")
    List<TeamMember> findByEmailIgnoreCaseWithProject(@Param("email") String email);
}
