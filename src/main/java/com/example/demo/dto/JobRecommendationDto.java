package com.example.demo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationDto {
    private String id;
    private String title;
    private String company;
    private String location;
    private String experience;
    private String education;
    private String employmentType;
    private String salary;
    private LocalDate deadline;
    private String url;
    private List<String> keywords;
    private LocalDate postedDate;
    private Integer matchScore;

    // 공공데이터 API 추가 필드
    private String description;     // 공고 설명
    private String requirements;    // 지원자격
    private String benefits;        // 우대사항
    private String recruitCount;    // 채용인원
}
