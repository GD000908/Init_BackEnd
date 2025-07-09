package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSpecDto {
    private SpecProfileDto profile;
    private SpecCareerStatsDto careerStats;
    private List<SpecCareerDto> workExperiences;
    private List<SpecEducationDto> educations;
    private List<SpecSkillDto> skills;
    private List<SpecCertificateDto> certificates;
    private List<SpecLanguageDto> languages;
    private List<SpecProjectDto> projects;
    private List<SpecActivityDto> activities;
    private List<SpecLinkDto> links;
    private List<SpecMilitaryDto> militaries;
}
