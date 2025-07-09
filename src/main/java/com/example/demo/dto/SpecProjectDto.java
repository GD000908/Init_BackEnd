package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecProjectDto {
    private Long id;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private String technologies;
    private String url;
    private Integer displayOrder;
}
