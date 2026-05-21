package com.example.career.dto;

import com.example.career.model.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String prenom;
    private String nom;
    private String email;
    private Role role;
    private boolean firstLogin;
    private String phone;
    private String company;
    private String position;
    private String currentRole;
    private String cvPath;
}
