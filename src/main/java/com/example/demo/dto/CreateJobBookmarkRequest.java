package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJobBookmarkRequest {

    @NotNull(message = "채용공고 ID는 필수입니다")
    private Long jobPostingId;

    private String memo;

    // 🔥 userId는 헤더에서 받아서 Controller에서 설정하므로 validation 제거
    private Long userId; // 헤더에서 설정될 필드
}