package com.example.demo.dto;

import com.example.demo.entity.Skill;
import lombok.*;

public class SkillDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String category;
        private Integer proficiencyLevel;

        public static Response from(Skill skill) {
            return Response.builder()
                    .id(skill.getId())
                    .name(skill.getName())
                    .category(skill.getCategory())
                    .proficiencyLevel(skill.getProficiencyLevel())
                    .build();
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String name;
        private String category;
        private Integer proficiencyLevel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private Long id;
        private String name;
        private String category;
        private Integer proficiencyLevel;
    }
}