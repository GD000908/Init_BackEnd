package com.example.demo.dto;

import com.example.demo.entity.WorkExperience;
import lombok.*;
import java.time.LocalDate;

public class WorkExperienceDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String company;
        private String position;
        private String department;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isCurrent;
        private String description;
        private String achievements;
        private String employmentType;

        public static Response from(WorkExperience workExperience) {
            return Response.builder()
                    .id(workExperience.getId())
                    .company(workExperience.getCompany())
                    .position(workExperience.getPosition())
                    .department(workExperience.getDepartment())
                    .startDate(workExperience.getStartDate())
                    .endDate(workExperience.getEndDate())
                    .isCurrent(workExperience.getIsCurrent())
                    .description(workExperience.getDescription())
                    .achievements(workExperience.getAchievements())
                    .employmentType(workExperience.getEmploymentType())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String company;
        private String position;
        private String department;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isCurrent;
        private String description;
        private String achievements;
        private String employmentType;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private Long id;
        private String company;
        private String position;
        private String department;
        private LocalDate startDate;
        private LocalDate endDate;
        private Boolean isCurrent;
        private String description;
        private String achievements;
        private String employmentType;
    }
}