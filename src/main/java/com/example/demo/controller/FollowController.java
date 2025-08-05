package com.example.demo.controller;

import com.example.demo.dto.ApiResponse;
import com.example.demo.dto.FollowDto;
import com.example.demo.dto.FollowStatusWithStatsDto;
import com.example.demo.dto.FollowToggleResponse;
import com.example.demo.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    /**
     * 팔로우 토글 (팔로우/언팔로우) - 프론트엔드 JSON 요청 지원
     */
    @PostMapping("/toggle")
    public ResponseEntity<ApiResponse<FollowToggleResponse>> toggleFollow(
            @RequestParam(required = false) Long followerId,
            @RequestParam(required = false) Long followingId,
            @RequestBody(required = false) Map<String, Long> requestBody) {

        log.info("=== 팔로우 토글 요청 ===");

        // 🔥 JSON body 우선, Query Parameter fallback
        Long actualFollowerId = requestBody != null && requestBody.get("followerId") != null
                ? requestBody.get("followerId") : followerId;
        Long actualFollowingId = requestBody != null && requestBody.get("followingId") != null
                ? requestBody.get("followingId") : followingId;

        log.info("🎯 매개변수 확인: followerId={}, followingId={}", actualFollowerId, actualFollowingId);

        if (actualFollowerId == null || actualFollowingId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.<FollowToggleResponse>builder()
                    .success(false)
                    .message("사용자 ID는 필수입니다")
                    .build());
        }

        if (actualFollowerId.equals(actualFollowingId)) {
            return ResponseEntity.badRequest().body(ApiResponse.<FollowToggleResponse>builder()
                    .success(false)
                    .message("자기 자신을 팔로우할 수 없습니다")
                    .build());
        }

        try {
            FollowToggleResponse result = followService.toggleFollow(actualFollowerId, actualFollowingId);
            return ResponseEntity.ok(ApiResponse.<FollowToggleResponse>builder()
                    .success(true)
                    .data(result)
                    .message("팔로우 토글 성공")
                    .build());
        } catch (Exception e) {
            log.error("❌ 팔로우 토글 실패: followerId={}, followingId={}", actualFollowerId, actualFollowingId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<FollowToggleResponse>builder()
                    .success(false)
                    .message("팔로우 처리 중 오류: " + e.getMessage())
                    .build());
        }
    }

    /**
     * 팔로우 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Boolean>> checkFollowStatus(
            @RequestParam Long followerId,
            @RequestParam Long followingId) {

        log.info("팔로우 상태 확인 요청: followerId={}, followingId={}", followerId, followingId);

        try {
            boolean isFollowing = followService.checkFollowStatus(followerId, followingId);
            return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                    .success(true)
                    .data(isFollowing)
                    .message("팔로우 상태 조회 성공")
                    .build());
        } catch (Exception e) {
            log.error("팔로우 상태 확인 실패: followerId={}, followingId={}", followerId, followingId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Boolean>builder()
                    .success(false)
                    .data(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * 팔로우 상태와 통계 정보 조회 (모달용)
     */
    @GetMapping("/status-with-stats")
    public ResponseEntity<ApiResponse<FollowStatusWithStatsDto>> getFollowStatusWithStats(
            @RequestParam Long followerId,
            @RequestParam Long followingId) {

        log.info("🔍 팔로우 상태 및 통계 조회 요청: followerId={}, followingId={}", followerId, followingId);

        try {
            FollowStatusWithStatsDto result = followService.getFollowStatusWithStats(followerId, followingId);
            return ResponseEntity.ok(ApiResponse.<FollowStatusWithStatsDto>builder()
                    .success(true)
                    .data(result)
                    .message("팔로우 상태 및 통계 조회 성공")
                    .build());
        } catch (Exception e) {
            log.error("❌ 팔로우 상태 및 통계 조회 실패: followerId={}, followingId={}", followerId, followingId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<FollowStatusWithStatsDto>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * 팔로잉 목록 조회
     */
    @GetMapping("/following")  // 🔥 URL 수정
    public ResponseEntity<ApiResponse<Page<FollowDto>>> getFollowingList(
            @RequestParam Long userId,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {

        log.info("팔로잉 목록 조회 요청: userId={}", userId);

        try {
            Page<FollowDto> follows = followService.getFollowingList(userId, currentUserId, pageable);
            return ResponseEntity.ok(ApiResponse.<Page<FollowDto>>builder()
                    .success(true)
                    .data(follows)
                    .build());
        } catch (Exception e) {
            log.error("팔로잉 목록 조회 실패: userId={}", userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Page<FollowDto>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * 팔로워 목록 조회
     */
    @GetMapping("/{userId}/followers")
    public ResponseEntity<ApiResponse<Page<FollowDto>>> getFollowersList(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId,
            Pageable pageable) {

        log.info("팔로워 목록 조회 요청: userId={}", userId);

        try {
            Page<FollowDto> followers = followService.getFollowersList(userId, currentUserId, pageable);
            return ResponseEntity.ok(ApiResponse.<Page<FollowDto>>builder()
                    .success(true)
                    .data(followers)
                    .build());
        } catch (Exception e) {
            log.error("팔로워 목록 조회 실패: userId={}", userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Page<FollowDto>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }

    /**
     * 팔로우 추천 사용자 조회
     */
    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<ApiResponse<Page<FollowDto.UserInfoDto>>> getFollowRecommendations(
            @PathVariable Long userId,
            Pageable pageable) {

        log.info("팔로우 추천 조회 요청: userId={}", userId);

        try {
            Page<FollowDto.UserInfoDto> recommendations = followService.getFollowRecommendations(userId, pageable);
            return ResponseEntity.ok(ApiResponse.<Page<FollowDto.UserInfoDto>>builder()
                    .success(true)
                    .data(recommendations)
                    .build());
        } catch (Exception e) {
            log.error("팔로우 추천 조회 실패: userId={}", userId, e);
            return ResponseEntity.badRequest().body(ApiResponse.<Page<FollowDto.UserInfoDto>>builder()
                    .success(false)
                    .message(e.getMessage())
                    .build());
        }
    }
}