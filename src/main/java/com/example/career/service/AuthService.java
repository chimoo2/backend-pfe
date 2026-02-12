package com.example.career.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.career.model.User;
import com.example.career.repository.UserRepository;
import com.example.career.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    
    public User register(User user) {
        // normalize email to avoid case/whitespace mismatches
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé !");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return userRepository.save(user);
    }

    
    public String login(String email, String password) {

        // normalize email before lookup
        if (email != null) email = email.trim().toLowerCase();

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        // generate JWT token
        String token = jwtService.generateToken(user.getEmail());
        return token;
    }

   
    public String forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

      
        String token = UUID.randomUUID().toString();

        user.setResetToken(token);
        userRepository.save(user);

       
        return "Token de réinitialisation : " + token;
    }

    
    public String resetPassword(String token, String newPassword) {

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);

        userRepository.save(user);

        return "Mot de passe mis à jour avec succès";
    }

    public java.util.Optional<com.example.career.model.User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
