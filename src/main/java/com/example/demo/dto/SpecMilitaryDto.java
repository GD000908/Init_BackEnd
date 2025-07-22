package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecMilitaryDto {
    private Long id;
    private String serviceType;
    private String militaryBranch;
    private String rank;
    private String startDate;
    private String endDate;
    private String exemptionReason;
}
