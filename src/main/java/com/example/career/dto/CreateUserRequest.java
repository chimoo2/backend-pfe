package com.example.career.dto;

import com.example.career.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateUserRequest {
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;

    // Role can be ROLE_USER or ROLE_ADMIN
    private Role role = Role.ROLE_USER;

    // Rôle métier actuel (ex: "Senior Developer")
    private String currentRole;
}
