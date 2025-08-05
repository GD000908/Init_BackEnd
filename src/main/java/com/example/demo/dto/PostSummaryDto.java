package com.example.demo.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostSummaryDto {
    private Long id;
    private String content;
    private String imageUrl;  // 🔥 이미지 URL 필드 추가
    private List<String> hashtags;
    private int likesCount;
    private int commentsCount;
    private String timeAgo;
    private String jobCategory;
    private String topicCategory;

    private AuthorDto author;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDto {
        private String name;
        private String avatar;
    }
}
