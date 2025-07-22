package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecLinkDto {
    private Long id;
    private String title;
    private String url;
    private String type;
    private Integer displayOrder;
}
