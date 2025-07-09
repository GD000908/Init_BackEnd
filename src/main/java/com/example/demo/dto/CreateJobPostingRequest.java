package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobPostingRequest {

    @NotBlank(message = "제목은 필수입니다")
    private String title;

    @NotNull(message = "시작일은 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "마감일은 필수입니다")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String location;
    private String position;
    private String salary;
    private String color;
    private String description;
    private String company;
    private String department;
    private String experienceLevel;
    private String employmentType;
}