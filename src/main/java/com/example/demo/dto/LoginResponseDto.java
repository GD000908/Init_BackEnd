package com.example.demo.dto;

import com.example.demo.entity.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 성공 시 클라이언트에 반환될 사용자 정보를 담는 DTO
 */
@Getter
@AllArgsConstructor
public class LoginResponseDto {
    private Long id;       // 데이터베이스의 고유 ID (PK)
    private String userId; // 로그인 시 사용하는 아이디
    private String name;   // 사용자 이름
    private String token;  // JWT 토큰
    private UserRole role; // 🔥 사용자 역할 추가
}