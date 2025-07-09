package com.example.demo.dto;

import com.example.demo.entity.Language;
import lombok.*;

public class LanguageDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String name;
        private String proficiencyLevel;
        private String testName;
        private String testScore;

        public static Response from(Language language) {
            return Response.builder()
                    .id(language.getId())
                    .name(language.getName())
                    .proficiencyLevel(language.getProficiencyLevel())
                    .testName(language.getTestName())
                    .testScore(language.getTestScore())
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
        private String proficiencyLevel;
        private String testName;
        private String testScore;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private Long id;
        private String name;
        private String proficiencyLevel;
        private String testName;
        private String testScore;
    }
}