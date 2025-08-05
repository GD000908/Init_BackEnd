package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostDto {
    
    private Long id;
    private String content;
    private String imageUrl;
    private String jobCategory;
    private String topicCategory;
    private String status;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer bookmarksCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 작성자 정보
    private AuthorDto author;
    
    // 해시태그
    private List<String> hashtags;
    
    // 댓글 목록 (상세보기 시에만 포함)
    private List<CommentDto> commentsList;
    
    // 추가 정보 (조회 시에만 포함)
    private Boolean likedByMe; // 현재 사용자가 좋아요했는지 여부
    private Boolean bookmarkedByMe; // 현재 사용자가 북마크했는지 여부
    private String timeAgo; // "2시간 전" 형태의 시간 표시
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuthorDto {
        private Long id;
        private String name;
        private String avatar;
        private String jobTitle;
        private Boolean isFollowing; // 현재 사용자가 작성자를 팔로우하는지 여부
    }
}
