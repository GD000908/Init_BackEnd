package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecSkillDto {
    private Long id;
    private String name;
    private String category;
    private Integer level;
    private Integer displayOrder;
}
