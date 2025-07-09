package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DesiredConditionsDto {
    private Long id;
    private Long userId;
    private List<String> jobs;
    private List<String> locations;
    private String salary;
    private List<String> others;
}
