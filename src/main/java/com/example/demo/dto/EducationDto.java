package com.example.demo.dto;

import com.example.demo.entity.Education;
import lombok.*;

import java.time.LocalDate;

public class EducationDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String school;
        private String major;
        private String degree;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isCurrent;
        private Double gpa;
        private Double maxGpa;
        private String description;

        public static Response from(Education education) {
            return Response.builder()
                    .id(education.getId())
                    .school(education.getSchool())
                    .major(education.getMajor())
                    .degree(education.getDegree())
                    .startDate(education.getStartDate())
                    .endDate(education.getEndDate())
                    .isCurrent(education.getIsCurrent())
                    .gpa(education.getGpa())
                    .maxGpa(education.getMaxGpa())
                    .description(education.getDescription())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String school;
        private String major;
        private String degree;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isCurrent;
        private Double gpa;
        private Double maxGpa;
        private String description;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private Long id;
        private String school;
        private String major;
        private String degree;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isCurrent;
        private Double gpa;
        private Double maxGpa;
        private String description;
    }
}