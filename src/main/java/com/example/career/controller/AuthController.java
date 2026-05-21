package com.example.career.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.career.dto.AuthResponse;
import com.example.career.dto.ChangePasswordRequest;
import com.example.career.dto.LoginRequest;
import com.example.career.dto.RegisterRequest;
import com.example.career.dto.ResetPasswordRequest;
import com.example.career.dto.UserDto;
import com.example.career.mapper.UserMapper;
import com.example.career.model.User;
import com.example.career.service.AuthService;
import com.example.career.security.JwtService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> register(@Valid @RequestBody RegisterRequest req) {
        User user = new User();
        user.setPrenom(req.getPrenom());
        user.setNom(req.getNom());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());

        User saved = authService.register(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDto(saved));
    }

    @PostMapping("/register-public")
    public ResponseEntity<UserDto> registerPublic(@Valid @RequestBody RegisterRequest req) {
        User user = new User();
        user.setPrenom(req.getPrenom());
        user.setNom(req.getNom());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());

        User saved = authService.registerPublic(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDto(saved));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            User user = authService.login(req.getEmail(), req.getPassword());
            String token = jwtService.generateToken(user.getEmail());
            AuthResponse resp = new AuthResponse(token, jwtService.getJwtExpirationMs(), user.isFirstLogin());
            return ResponseEntity.ok(resp);
        } catch (RuntimeException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("error", ex.getMessage());
            return ResponseEntity.status(401).body(body);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(name = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Missing or invalid Authorization header"));
        }
        String token = authHeader.substring(7);
        String email = jwtService.validateTokenAndGetSubject(token);
        if (email == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Invalid or expired token"));
        }
        java.util.Optional<User> opt = authService.getUserByEmail(email);
        if (opt.isPresent()) {
            return ResponseEntity.ok(UserMapper.toDto(opt.get()));
        } else {
            return ResponseEntity.status(404).body(java.util.Map.of("error", "User not found"));
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        String result = authService.forgotPassword(email);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        String result = authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(name = "Authorization", required = false) String authHeader,
            @Valid @RequestBody ChangePasswordRequest req) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Missing or invalid Authorization header"));
        }
        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Les mots de passe ne correspondent pas"));
        }
        String token = authHeader.substring(7);
        String email = jwtService.validateTokenAndGetSubject(token);
        if (email == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Token invalide ou expiré"));
        }
        try {
            authService.changePassword(email, req.getOldPassword(), req.getNewPassword());
            return ResponseEntity.ok(Map.of("message", "Mot de passe mis à jour avec succès"));
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }
}
