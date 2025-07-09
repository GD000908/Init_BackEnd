package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

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
