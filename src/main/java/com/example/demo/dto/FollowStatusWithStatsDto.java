package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FollowStatusWithStatsDto {
    
    private Boolean isFollowing;
    private Boolean isMutualFollow;
    private Boolean canFollow; // 팔로우 가능 여부
    private Boolean isOwner; // 본인 프로필 여부
    
    // 대상 사용자의 통계 정보
    private Integer followersCount;
    private Integer followingCount;
    private Integer postsCount;
    
    private String message;
}
