package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecCareerDto {
    private Long id;
    private String company;
    private String department;
    private String position;
    private String startDate;
    private String endDate;
    private Boolean isCurrent;
    private String description;
    private Integer displayOrder;
}
