package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HomePageAllDataDto {
    private UserProfileDto profile;
    private DesiredConditionsDto conditions;
    private List<ApplicationStatusDto> applications;
    private List<TodoItemDto> todos;
    private HomePageStatsDto stats;
}
