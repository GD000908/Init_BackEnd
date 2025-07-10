package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.entity.UserRole;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final UserRepository userRepository;

    /**
     * 전체 사용자 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers(int page, int size, String search, String sortBy) {
        log.info("🔍 getAllUsers 호출됨 - page: {}, size: {}, search: '{}', sortBy: '{}'",
                page, size, search, sortBy);

        Sort sort = Sort.by(Sort.Direction.DESC, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage;
        if (search != null && !search.trim().isEmpty()) {
            log.info("🔎 검색어로 사용자 조회: '{}'", search);
            userPage = userRepository.findByUserIdContainingOrEmailContainingOrNameContaining(
                    search, search, search, pageable);
        } else {
            log.info("📋 전체 사용자 조회");
            userPage = userRepository.findAll(pageable);
        }

        List<User> users = userPage.getContent();
        log.info("✅ 조회된 사용자 수: {} / 전체: {}", users.size(), userPage.getTotalElements());

        // 각 사용자 정보 로깅
        users.forEach(user -> {
            log.debug("👤 User: id={}, userId='{}', name='{}', email='{}', role={}, isActive={}",
                    user.getId(), user.getUserId(), user.getName(), user.getEmail(),
                    user.getRole(), user.getIsActive());
        });

        return users;
    }

    /**
     * 사용자 상태 변경 (활성화/비활성화)
     */
    @Transactional
    public void updateUserStatus(Long userId, boolean isActive) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 관리자 계정은 비활성화할 수 없음
        if (user.getRole() == UserRole.ADMIN && !isActive) {
            throw new RuntimeException("관리자 계정은 비활성화할 수 없습니다.");
        }

        user.setIsActive(isActive);
        userRepository.save(user);

        log.info("사용자 상태 변경: {} -> {}", user.getUserId(), isActive ? "활성" : "비활성");
    }

    /**
     * 특정 사용자 상세 정보 조회
     */
    @Transactional(readOnly = true)
    public User getUserDetail(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    /**
     * 대시보드 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // 전체 사용자 수
        long totalUsers = userRepository.count();
        stats.put("totalUsers", totalUsers);

        // 활성 사용자 수
        long activeUsers = userRepository.countByIsActive(true);
        stats.put("activeUsers", activeUsers);

        // 비활성 사용자 수
        long deactivatedUsers = userRepository.countByIsActive(false);
        stats.put("deactivatedUsers", deactivatedUsers);

        // 오늘 가입자 수
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long todaySignups = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
        stats.put("todaySignups", todaySignups);

        // 최근 7일 가입자 추이
        List<Map<String, Object>> dailyGrowth = getDailyGrowthStats();
        stats.put("dailyGrowth", dailyGrowth);

        return stats;
    }

    /**
     * 최근 7일간 일별 가입자 통계
     */
    private List<Map<String, Object>> getDailyGrowthStats() {
        List<Map<String, Object>> dailyStats = new java.util.ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = LocalDateTime.now().minusDays(i);
            LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            long userCount = userRepository.countByCreatedAtBefore(endOfDay);

            Map<String, Object> dayStats = new HashMap<>();
            dayStats.put("date", date.toLocalDate().toString());
            dayStats.put("users", userCount);
            dailyStats.add(dayStats);
        }

        return dailyStats;
    }

    /**
     * 사용자 삭제 (실제로는 비활성화)
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 관리자 계정은 삭제할 수 없음
        if (user.getRole() == UserRole.ADMIN) {
            throw new RuntimeException("관리자 계정은 삭제할 수 없습니다.");
        }

        user.setIsActive(false);
        userRepository.save(user);

        log.info("사용자 삭제(비활성화): {}", user.getUserId());
    }

    /**
     * 사용자 검색
     */
    @Transactional(readOnly = true)
    public List<User> searchUsers(String keyword, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage = userRepository.findByUserIdContainingOrEmailContainingOrNameContaining(
                keyword, keyword, keyword, pageable);

        return userPage.getContent();
    }
}