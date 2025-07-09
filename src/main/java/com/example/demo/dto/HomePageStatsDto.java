package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomePageStatsDto {
    private int totalApplications;
    private int documentPassed;
    private int finalPassed;
    private int rejected;
    
    private int resumeCount;
    private int coverLetterCount;
    private int bookmarkedCompanies;
    private int deadlinesApproaching;
    
    private ProfileCompletionDto profileCompletion;
}
