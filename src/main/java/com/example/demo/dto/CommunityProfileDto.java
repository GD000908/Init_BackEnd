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
public class CommunityProfileDto {
    
    private Long id;
    private Long userId;
    private String displayName;

    private String bio;
    private String jobTitle;
    private String company;
    private String location;
    private String profileImageUrl;
    private String coverImageUrl;
    private Boolean isPublic;
    private Boolean allowFollow;
    private Integer postsCount;
    private Integer followersCount;
    private Integer followingCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 추가 정보 (조회 시에만 포함)
    private Boolean isFollowing; // 현재 사용자가 이 프로필을 팔로우하는지 여부
    private Boolean isMutualFollow; // 상호 팔로우인지 여부
    private Boolean isOwner; // 현재 사용자가 이 프로필의 소유자인지 여부
}
