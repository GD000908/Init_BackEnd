package com.example.demo.dto;

import com.example.demo.entity.CoverLetter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.demo.dto.CoverLetterQuestionDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
public class CoverLetterDto {

    private Long id;
    private Long userId;
    private String title;
    private Boolean isDraft = true;
    private String company;
    private String position;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CoverLetterQuestionDto> questions; // 외부 클래스로 변경

    public static CoverLetterDto fromEntity(CoverLetter entity) {
        CoverLetterDto dto = new CoverLetterDto();
        dto.setId(entity.getId());
        dto.setUserId(entity.getUserId());
        dto.setTitle(entity.getTitle());
        dto.setIsDraft(entity.getIsDraft());
        dto.setCompany(entity.getCompany());
        dto.setPosition(entity.getPosition());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        dto.setQuestions(
                entity.getQuestions().stream()
                        .map(CoverLetterQuestionDto::fromEntity)
                        .collect(Collectors.toList())
        );

        return dto;
    }
}
