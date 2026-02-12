package com.example.career.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String prenom;
    private String nom;
    private String email;
    private String password;
}
