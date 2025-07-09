package com.example.demo.dto;



import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobRecommendationRequestDto {
    private List<String> keywords;
    private List<String> locations;
}