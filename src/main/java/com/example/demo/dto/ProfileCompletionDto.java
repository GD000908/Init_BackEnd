package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCompletionDto {
    private boolean basicInfo;
    private boolean desiredConditions;
    private boolean workExperience;
    private boolean education;
    private boolean certificate;
    private boolean language;
    private boolean skill;
    private boolean link;
    private boolean military;
    private boolean portfolio;
    private int completionPercentage;
}
