package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecCertificateDto {
    private Long id;
    private String name;
    private String organization1;
    private String acquisitionDate;
    private String expirationDate;
    private String certificateNumber;
    private Integer displayOrder;
}
