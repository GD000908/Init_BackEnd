package com.example.demo.controller;

import com.example.demo.dto.AdminUserDto;
import com.example.demo.dto.ApiResponse;
import com.example.demo.entity.User;
import com.example.demo.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final AdminService adminService;

    /**
     * 전체 사용자 목록 조회 (DTO 변환)
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<AdminUserDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy) {

        try {
            log.info("🎯 관리자 사용자 목록 API 호출 - page: {}, size: {}, search: '{}', sortBy: '{}'",
                    page, size, search, sortBy);

            List<User> users = adminService.getAllUsers(page, size, search, sortBy);

            // 🔥 User 엔티티를 AdminUserDto로 변환 (순환 참조 방지)
            List<AdminUserDto> userDtos = users.stream()
                    .map(AdminUserDto::from)
                    .collect(Collectors.toList());

            log.info("📤 변환된 사용자 DTO 수: {}", userDtos.size());

            ApiResponse<List<AdminUserDto>> response = ApiResponse.success("사용자 목록 조회 성공", userDtos);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("❌ 사용자 목록 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자 활성화/비활성화
     */
    @PatchMapping("/users/{userId}/status")
    public ResponseEntity<ApiResponse<String>> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam boolean isActive) {

        try {
            adminService.updateUserStatus(userId, isActive);
            String message = isActive ? "사용자가 활성화되었습니다." : "사용자가 비활성화되었습니다.";
            return ResponseEntity.ok(ApiResponse.success(message));
        } catch (Exception e) {
            log.error("❌ 사용자 상태 변경 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 상태 변경에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 사용자 상세 정보 조회
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<AdminUserDto>> getUserDetail(@PathVariable Long userId) {
        try {
            User user = adminService.getUserDetail(userId);
            AdminUserDto userDto = AdminUserDto.from(user);
            return ResponseEntity.ok(ApiResponse.success("사용자 정보 조회 성공", userDto));
        } catch (Exception e) {
            log.error("❌ 사용자 정보 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 정보 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 대시보드 통계 정보 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatistics() {
        try {
            Map<String, Object> statistics = adminService.getStatistics();
            return ResponseEntity.ok(ApiResponse.success("통계 정보 조회 성공", statistics));
        } catch (Exception e) {
            log.error("❌ 통계 정보 조회 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("통계 정보 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자 삭제 (실제로는 비활성화)
     */
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long userId) {
        try {
            adminService.deleteUser(userId);
            return ResponseEntity.ok(ApiResponse.success("사용자가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("❌ 사용자 삭제 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    /**
     * 사용자 검색
     */
    @GetMapping("/users/search")
    public ResponseEntity<ApiResponse<List<AdminUserDto>>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            List<User> users = adminService.searchUsers(keyword, page, size);

            List<AdminUserDto> userDtos = users.stream()
                    .map(AdminUserDto::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success("사용자 검색 성공", userDtos));
        } catch (Exception e) {
            log.error("❌ 사용자 검색 실패", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("사용자 검색에 실패했습니다: " + e.getMessage()));
        }
    }
}