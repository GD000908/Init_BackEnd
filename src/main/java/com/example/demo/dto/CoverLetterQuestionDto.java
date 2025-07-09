package com.example.demo.dto;

import com.example.demo.entity.CoverLetterQuestion;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CoverLetterQuestionDto {
    private Long id;
    private String title;
    private String content;
    private Integer wordLimit = 500;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CoverLetterQuestionDto fromEntity(CoverLetterQuestion entity) {
        CoverLetterQuestionDto dto = new CoverLetterQuestionDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setContent(entity.getContent());
        dto.setWordLimit(entity.getWordLimit());
        dto.setDisplayOrder(entity.getDisplayOrder());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
