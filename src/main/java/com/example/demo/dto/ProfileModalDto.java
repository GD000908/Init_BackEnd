package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileModalDto {
    
    private Long id; // Profile ID
    private Long userId; // User ID
    private String displayName;
    private String bio;
    private String jobTitle;
    private String company;
    private String location;
    private String profileImageUrl;
    
    // 통계 정보
    private Integer postsCount;
    private Integer followersCount;
    private Integer followingCount;
    
    // 관계 정보
    private Boolean isFollowing; // 현재 사용자가 이 프로필을 팔로우하는지 여부
    private Boolean isMutualFollow; // 상호 팔로우인지 여부
    private Boolean isOwner; // 현재 사용자가 이 프로필의 소유자인지 여부
    private Boolean allowFollow; // 팔로우 허용 여부
    private Boolean isPublic; // 공개 프로필 여부
}
