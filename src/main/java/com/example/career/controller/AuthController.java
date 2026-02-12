package com.example.career.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.career.model.User;
import com.example.career.service.AuthService;
import com.example.career.dto.RegisterRequest;
import com.example.career.dto.LoginRequest;
import com.example.career.security.JwtService;
import com.example.career.dto.AuthResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            User user = new User();
            user.setPrenom(req.getPrenom());
            user.setNom(req.getNom());
            user.setEmail(req.getEmail());
            user.setPassword(req.getPassword());

            User saved = authService.register(user);
            return new ResponseEntity<>(saved, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            Map<String, String> body = new HashMap<>();
            body.put("error", ex.getMessage());
            return ResponseEntity.badRequest().body(body);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            String token = authService.login(req.getEmail(), req.getPassword());
            AuthResponse resp = new AuthResponse(token, jwtService.getJwtExpirationMs());
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
            return ResponseEntity.ok(opt.get());
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
    public ResponseEntity<String> resetPassword(@RequestParam String token,
                                                @RequestParam String newPassword) {
        String result = authService.resetPassword(token, newPassword);
        return ResponseEntity.ok(result);
    }
}
