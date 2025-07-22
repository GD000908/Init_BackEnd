package com.example.demo.dto;

import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 관리자 페이지에서 사용할 사용자 정보 DTO
 * 순환 참조 방지 및 불필요한 정보 제외
 */
@Getter
@Setter
public class AdminUserDto {
    private Long id;
    private String userId;
    private String email;
    private String name;
    private String phone;
    private Boolean isActive;
    private UserRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 🔥 User 엔티티에서 DTO로 변환하는 생성자
    public AdminUserDto(User user) {
        this.id = user.getId();
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.isActive = user.getIsActive();
        this.role = user.getRole();
        this.createdAt = user.getCreatedAt();
        this.updatedAt = user.getUpdatedAt();
    }

    // 🔥 정적 팩토리 메서드
    public static AdminUserDto from(User user) {
        return new AdminUserDto(user);
    }
}