package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecLanguageDto {
    private Long id;
    private String language;
    private String level;
    private String testName;
    private String score;
    private String acquisitionDate;
    private Integer displayOrder;
}
