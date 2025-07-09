package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final UserRepository userRepository;

    /**
     * 사용자 ID로 사용자를 조회하고 검증합니다.
     * @param userId 검증할 사용자 ID
     * @return 검증된 User 엔티티
     * @throws RuntimeException 사용자가 존재하지 않거나 비활성화된 경우
     */
    public User validateUser(Long userId) {
        if (userId == null) {
            throw new RuntimeException("사용자 ID가 필요합니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));

        if (!user.getIsActive()) {
            throw new RuntimeException("비활성화된 사용자입니다.");
        }

        return user;
    }

    /**
     * 요청한 사용자가 해당 리소스에 접근할 권한이 있는지 확인합니다.
     * @param requestUserId 요청한 사용자 ID
     * @param resourceUserId 리소스 소유자 사용자 ID
     * @throws RuntimeException 권한이 없는 경우
     */
    public void validateUserAccess(Long requestUserId, Long resourceUserId) {
        if (!requestUserId.equals(resourceUserId)) {
            throw new RuntimeException("해당 리소스에 접근할 권한이 없습니다.");
        }
    }
}