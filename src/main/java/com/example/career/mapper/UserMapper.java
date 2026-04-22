package com.example.career.mapper;

import com.example.career.dto.UserDto;
import com.example.career.model.User;

public class UserMapper {

    public static UserDto toDto(User user) {
        if (user == null) return null;
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setPrenom(user.getPrenom());
        dto.setNom(user.getNom());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole());
        dto.setPhone(user.getPhone());
        dto.setCompany(user.getCompany());
        dto.setPosition(user.getPosition());
        dto.setCvPath(user.getCvPath());
        return dto;
    }
}
