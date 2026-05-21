package com.example.career.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.career.model.Role;
import com.example.career.model.User;
import com.example.career.repository.UserRepository;
import com.example.career.security.JwtService;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Value("${app.frontend.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
    }

    
    public User register(User user) {
        normalizeEmail(user);
        ensureEmailNotTaken(user.getEmail());

        // Default to regular user unless explicitly set to ADMIN
        if (user.getRole() == null) {
            user.setRole(Role.ROLE_USER);
        }

        // Accounts created by admin always require a password change on first login
        user.setFirstLogin(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User registerPublic(User user) {
        normalizeEmail(user);
        ensureEmailNotTaken(user.getEmail());

        user.setRole(Role.ROLE_USER);
        user.setFirstLogin(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    private void normalizeEmail(User user) {
        if (user.getEmail() != null) {
            user.setEmail(user.getEmail().trim().toLowerCase());
        }
    }

    private void ensureEmailNotTaken(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email déjà utilisé !");
        }
    }

    
    public User login(String email, String password) {

        // normalize email before lookup
        if (email != null) email = email.trim().toLowerCase();

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Mot de passe incorrect");
        }

        return user;
    }

   
    public String forgotPassword(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String token = UUID.randomUUID().toString();
        user.setResetToken(token);
        userRepository.save(user);

        // send email with reset link
        String resetUrl = String.format("%s/reset-password?token=%s", frontendBaseUrl, token);
        emailService.sendResetPasswordEmail(email, resetUrl);
        //Cela récupère l’URL du frontend React.
        //Ensuite, il génère un token de réinitialisation unique et l’associe à l’utilisateur dans la base de données.
        //Enfin, il envoie un email à l’utilisateur avec un lien contenant le token pour réinitialiser son mot de passe.
        

        return "Si cet email existe, un message de réinitialisation a été envoyé.";
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

    public void changePassword(String email, String oldPassword, String newPassword) {
        if (email != null) email = email.trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Mot de passe actuel incorrect");
        }

        if (newPassword == null || newPassword.length() < 6) {
            throw new RuntimeException("Le nouveau mot de passe doit contenir au moins 6 caractères");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setFirstLogin(false);
        userRepository.save(user);
    }
}

