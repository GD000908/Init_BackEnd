package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowToggleRequest {
    
    private Long followerId;    // 팔로우하는 사람 ID
    private Long followingId;   // 팔로우받는 사람 ID
}
