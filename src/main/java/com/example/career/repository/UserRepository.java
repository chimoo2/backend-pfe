package com.example.career.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.career.model.User;
import java.util.Optional; 

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String token);
}
