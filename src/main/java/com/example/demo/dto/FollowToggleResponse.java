package com.example.demo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowToggleResponse {
    
    private Boolean success;
    private Boolean following; // 현재 팔로우 상태
    private String message;
    private Integer followersCount; // 업데이트된 팔로워 수
    private Integer followingCount; // 업데이트된 팔로잉 수
}
