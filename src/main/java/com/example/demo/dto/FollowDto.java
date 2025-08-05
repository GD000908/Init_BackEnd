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
public class FollowDto {
    
    private Long id;
    private LocalDateTime createdAt;
    
    // 팔로워 정보 (누가 팔로우했는지)
    private UserInfoDto follower;
    
    // 팔로잉 정보 (누구를 팔로우했는지)
    private UserInfoDto following;
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserInfoDto {
        private Long id;
        private Long userId;
        private String displayName;
        private String profileImageUrl;
        private String jobTitle;
        private String company;
        private Integer followersCount;
        private Integer followingCount;
        private Integer postsCount;
        private Boolean isFollowing; // 현재 사용자가 이 사용자를 팔로우하는지 여부
    }
}
