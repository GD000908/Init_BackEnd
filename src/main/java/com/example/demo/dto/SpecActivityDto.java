package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecActivityDto {
    private Long id;
    private String name;
    private String organization;
    private String startDate;
    private String endDate;
    private String description;
    private Integer displayOrder;
}
