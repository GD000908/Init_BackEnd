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
public class HomePageAllDataDto {
    private UserProfileDto profile;
    private DesiredConditionsDto conditions;
    private List<ApplicationStatusDto> applications;
    private List<TodoItemDto> todos;
    private HomePageStatsDto stats;
}
