package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecEducationDto {
    private Long id;
    private String school;
    private String major;
    private String degree;
    private String startDate;
    private String endDate;
    private Boolean isCurrent;
    private Double gpa;
    private Double maxGpa;
    private String description;
    private Integer displayOrder;
}
