package com.example.career.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequiredSkillDto {
    private Long id;
    private String skillName;
    private Integer criticality;
    // optional taxonomy metadata
    private String domain;
    private String family;
    private String category;
    private String type;
}
