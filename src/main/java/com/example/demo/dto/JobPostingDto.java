package com.example.demo.dto;

import com.example.demo.entity.JobPosting;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostingDto {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String location;
    private String position;
    private String salary;
    private String color;
    private JobPosting.JobStatus status;
    private String description;
    private String company;
    private String department;
    private String experienceLevel;
    private String employmentType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static JobPostingDto from(JobPosting jobPosting) {
        return JobPostingDto.builder()
                .id(jobPosting.getId())
                .title(jobPosting.getTitle())
                .startDate(jobPosting.getStartDate())
                .endDate(jobPosting.getEndDate())
                .location(jobPosting.getLocation())
                .position(jobPosting.getPosition())
                .salary(jobPosting.getSalary())
                .color(jobPosting.getColor())
                .status(jobPosting.calculateStatus()) // 동적으로 상태 계산
                .description(jobPosting.getDescription())
                .company(jobPosting.getCompany())
                .department(jobPosting.getDepartment())
                .experienceLevel(jobPosting.getExperienceLevel())
                .employmentType(jobPosting.getEmploymentType())
                .createdAt(jobPosting.getCreatedAt())
                .updatedAt(jobPosting.getUpdatedAt())
                .build();
    }
}