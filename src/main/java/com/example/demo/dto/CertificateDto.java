package com.example.demo.dto;

import com.example.demo.entity.Certificate;
import lombok.*;
import java.time.LocalDate;

public class CertificateDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String organization;
        private LocalDate acquisitionDate;
        private LocalDate expirationDate;
        private String certificateNumber;
        private Integer displayOrder;

        public static Response from(Certificate certificate) {
            return Response.builder()
                    .id(certificate.getId())
                    .name(certificate.getName())
                    .organization(certificate.getOrganization())
                    .acquisitionDate(certificate.getAcquisitionDate())
                    .expirationDate(certificate.getExpirationDate())
                    .certificateNumber(certificate.getCertificateNumber())
                    .displayOrder(certificate.getDisplayOrder())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String name;
        private String organization;
        private LocalDate acquisitionDate;
        private LocalDate expirationDate;
        private String certificateNumber;
        private Integer displayOrder;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private Long id;
        private String name;
        private String organization;
        private LocalDate acquisitionDate;
        private LocalDate expirationDate;
        private String certificateNumber;
        private Integer displayOrder;
    }
}