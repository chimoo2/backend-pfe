package com.example.career.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.career.repository.UserRepository;
import com.example.career.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public java.util.Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public java.util.List<User> findAll() {
        return userRepository.findAll();
    }

    public java.util.Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public User updateUser(Long id, User updated) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        existing.setPrenom(updated.getPrenom());
        existing.setNom(updated.getNom());
        existing.setEmail(updated.getEmail());
        existing.setRole(updated.getRole());
        if (updated.getPassword() != null && !updated.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updated.getPassword()));
        }
        return userRepository.save(existing);
    }
}
