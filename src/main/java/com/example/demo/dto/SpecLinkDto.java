package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

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
