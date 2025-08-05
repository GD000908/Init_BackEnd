package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommentDto {
    
    private Long id;
    private Long postId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 작성자 정보
    private AuthorDto author;
    
    // 추가 정보 (조회 시에만 포함)
    private String timeAgo; // "2시간 전" 형태의 시간 표시

    private PostSummaryDto post;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDto {
        private Long id;
        private Long userId;
        private String name;
        private String avatar;
        private String title;
    }
}
