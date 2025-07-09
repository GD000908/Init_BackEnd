package com.example.demo.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecProfileDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String location;
    private String careerLevel;
    private String jobTitle;
    private String introduction;
    private List<String> skills;
    private String profileImage;
    private LocalDate birthDate;
}
