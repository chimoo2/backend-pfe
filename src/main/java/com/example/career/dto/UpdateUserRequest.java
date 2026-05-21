package com.example.career.dto;

import com.example.career.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Email invalide")
    private String email;

    // Le mot de passe est optionnel pour l'update
    private String password;

    private String phone;
    private String company;

    private Role role = Role.ROLE_USER;

    // Rôle métier actuel (ex: "Senior Developer")
    private String currentRole;
}
