package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatusDto {
    private Long id;
    private Long userId;
    private String company;
    private String category;
    private String status;
    private LocalDate deadline;
}
