package com.example.demo.dto;

import com.example.demo.entity.Resume;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ResumeDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListResponse {
        private Long id;
        private String title;
        private String summary;
        private Boolean isPrimary;
        private Boolean isPublic;
        private String jobCategory;
        private String companyType;
        private String preferredLocation;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static ListResponse from(Resume resume) {
            return ListResponse.builder()
                    .id(resume.getId())
                    .title(resume.getTitle())
                    .summary(resume.getSummary())
                    .isPrimary(resume.getIsPrimary())
                    .isPublic(resume.getIsPublic())
                    .jobCategory(resume.getJobCategory())
                    .companyType(resume.getCompanyType())
                    .preferredLocation(resume.getPreferredLocation())
                    .createdAt(resume.getCreatedAt())
                    .updatedAt(resume.getUpdatedAt())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String summary;
        private Boolean isPrimary;
        private Boolean isPublic;

        // 개인정보
        private String fullName;
        private String email;
        private String phone;
        private String address;
        private String birthDate;
        private String gender;
        private String profileImageUrl;

        // URL 정보
        private String githubUrl;
        private String blogUrl;
        private String portfolioUrl;
        private String linkedinUrl;

        // 희망 직무 정보
        private String jobCategory;
        private String companyType;
        private String preferredLocation;
        private String jobPosition;

        // 연관 데이터
        private List<WorkExperienceDto.Response> workExperiences;
        private List<EducationDto.Response> educations;
        private List<SkillDto.Response> skills;
        private List<CertificateDto.Response> certificates;
        private List<LanguageDto.Response> languages;

        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(Resume resume) {
            return Response.builder()
                    .id(resume.getId())
                    .title(resume.getTitle())
                    .summary(resume.getSummary())
                    .isPrimary(resume.getIsPrimary())
                    .isPublic(resume.getIsPublic())
                    .fullName(resume.getFullName())
                    .email(resume.getEmail())
                    .phone(resume.getPhone())
                    .address(resume.getAddress())
                    .birthDate(resume.getBirthDate())
                    .gender(resume.getGender())
                    .profileImageUrl(resume.getProfileImageUrl())
                    .githubUrl(resume.getGithubUrl())
                    .blogUrl(resume.getBlogUrl())
                    .portfolioUrl(resume.getPortfolioUrl())
                    .linkedinUrl(resume.getLinkedinUrl())
                    .jobCategory(resume.getJobCategory())
                    .companyType(resume.getCompanyType())
                    .preferredLocation(resume.getPreferredLocation())
                    .jobPosition(resume.getJobPosition())
                    .workExperiences(resume.getWorkExperiences().stream()
                            .map(WorkExperienceDto.Response::from)
                            .collect(Collectors.toList()))
                    .educations(resume.getEducations().stream()
                            .map(EducationDto.Response::from)
                            .collect(Collectors.toList()))
                    .skills(resume.getSkills().stream()
                            .map(SkillDto.Response::from)
                            .collect(Collectors.toList()))
                    .certificates(resume.getCertificates().stream()
                            .map(CertificateDto.Response::from)
                            .collect(Collectors.toList()))
                    .languages(resume.getLanguages().stream()
                            .map(LanguageDto.Response::from)
                            .collect(Collectors.toList()))
                    .createdAt(resume.getCreatedAt())
                    .updatedAt(resume.getUpdatedAt())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String title;
        private String summary;
        private Boolean isPrimary;
        private Boolean isPublic;

        // 개인정보
        private String fullName;
        private String email;
        private String phone;
        private String address;
        private String birthDate;
        private String gender;
        private String profileImageUrl;

        // URL 정보
        private String githubUrl;
        private String blogUrl;
        private String portfolioUrl;
        private String linkedinUrl;

        // 희망 직무 정보
        private String jobCategory;
        private String companyType;
        private String preferredLocation;
        private String jobPosition;

        // 연관 데이터
        private List<WorkExperienceDto.CreateRequest> workExperiences;
        private List<EducationDto.CreateRequest> educations;
        private List<SkillDto.CreateRequest> skills;
        private List<CertificateDto.CreateRequest> certificates;
        private List<LanguageDto.CreateRequest> languages;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String title;
        private String summary;
        private Boolean isPrimary;
        private Boolean isPublic;

        // 개인정보
        private String fullName;
        private String email;
        private String phone;
        private String address;
        private String birthDate;
        private String gender;
        private String profileImageUrl;

        // URL 정보
        private String githubUrl;
        private String blogUrl;
        private String portfolioUrl;
        private String linkedinUrl;

        // 희망 직무 정보
        private String jobCategory;
        private String companyType;
        private String preferredLocation;
        private String jobPosition;

        // 연관 데이터
        private List<WorkExperienceDto.UpdateRequest> workExperiences;
        private List<EducationDto.UpdateRequest> educations;
        private List<SkillDto.UpdateRequest> skills;
        private List<CertificateDto.UpdateRequest> certificates;
        private List<LanguageDto.UpdateRequest> languages;
    }
}